package org.yangdai.kori.presentation.screen.template

import ai.koog.agents.utils.SuitableForIO
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.substring
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import knet.ConnectivityObserver
import knet.ai.AI
import knet.ai.GenerationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.component.note.AIAssistEvent
import org.yangdai.kori.presentation.component.note.ProcessedContent
import org.yangdai.kori.presentation.component.note.processMarkdown
import org.yangdai.kori.presentation.component.note.processTodo
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.Constants.LLMConfig.getLLMConfig
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalFoundationApi::class, ExperimentalUuidApi::class)
class TemplateViewModel(
    savedStateHandle: SavedStateHandle,
    private val dataStoreRepository: DataStoreRepository,
    private val noteRepository: NoteRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Screen.Template>()

    private val _templateEditingState = MutableStateFlow(TemplateEditingState())
    val editingState = _templateEditingState.asStateFlow()

    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    private val contentSnapshotFlow = snapshotFlow { contentState.text }
    private var oNote = NoteEntity(isTemplate = true)
    private val _isInitialized = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            if (route.id.isEmpty()) {
                val currentTime = Clock.System.now().toString()
                _templateEditingState.update {
                    it.copy(
                        id = Uuid.random().toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        noteType = NoteType.entries[route.noteType]
                    )
                }
                oNote = NoteEntity(isTemplate = true)
            } else {
                noteRepository.getNoteById(route.id)?.let { note ->
                    titleState.setTextAndPlaceCursorAtEnd(note.title)
                    contentState.setTextAndPlaceCursorAtEnd(note.content)
                    _templateEditingState.update {
                        it.copy(
                            id = note.id,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            noteType = note.noteType
                        )
                    }
                    oNote = note
                }
            }
            titleState.undoState.clearHistory()
            contentState.undoState.clearHistory()
            _isInitialized.update { true }
        }
    }

    val editorState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_MARKDOWN_LINT_ENABLED)
    ) { showLineNumber, isMarkdownLintEnabled ->
        EditorPaneState(
            isLineNumberVisible = showLineNumber,
            isMarkdownLintEnabled = isMarkdownLintEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EditorPaneState())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val processedContent = _isInitialized.filter { it }
        .flatMapLatest {
            combine(
                snapshotFlow { _templateEditingState.value.noteType },
                contentSnapshotFlow.debounce(100)
            ) { noteType, content ->
                when (noteType) {
                    NoteType.MARKDOWN -> {
                        val processed = processMarkdown(content.toString())
                        ProcessedContent.Markdown(processed)
                    }

                    NoteType.TODO -> {
                        val (undone, done) = processTodo(content.lines())
                        ProcessedContent.Todo(undone, done)
                    }

                    else -> ProcessedContent.Empty
                }
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ProcessedContent.Empty
        )

    fun updateNoteType(noteType: NoteType) {
        _templateEditingState.update {
            it.copy(noteType = noteType)
        }
    }

    fun saveOrUpdateNote() {
        if (_templateEditingState.value.isDeleted) return
        viewModelScope.launch {
            val newNote = NoteEntity(
                id = _templateEditingState.value.id,
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                createdAt = _templateEditingState.value.createdAt,
                updatedAt = Clock.System.now().toString(),
                isTemplate = true,
                noteType = _templateEditingState.value.noteType
            )
            if (oNote.id.isEmpty()) {
                if (newNote.title.isNotBlank() || newNote.content.isNotBlank()) {
                    val title = newNote.title
                        .ifBlank {
                            val defaultTitle =
                                "Template_${Clock.System.now().toEpochMilliseconds()}"
                            titleState.setTextAndPlaceCursorAtEnd(defaultTitle)
                            defaultTitle
                        }
                    noteRepository.insertNote(newNote.copy(title = title))
                    oNote = newNote.copy(title = title)
                }
            } else {
                if (oNote.title != newNote.title || oNote.content != newNote.content ||
                    oNote.noteType != newNote.noteType
                ) {
                    noteRepository.updateNote(newNote)
                    oNote = newNote
                }
            }
        }
    }

    fun deleteTemplate() {
        viewModelScope.launch {
            _templateEditingState.update { it.copy(isDeleted = true) }
            if (oNote.id.isNotEmpty()) {
                noteRepository.deleteNoteById(_templateEditingState.value.id)
            }
            _uiEventChannel.send(UiEvent.NavigateUp)
        }
    }

    /*----*/

    val showAI = combine(
        connectivityObserver.observe(),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_AI_ENABLED),
        snapshotFlow { _templateEditingState.value.noteType }
    ) { status, isAiEnabled, noteType ->
        status == ConnectivityObserver.Status.Connected && isAiEnabled && noteType != NoteType.Drawing
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    fun onAIAssistEvent(event: AIAssistEvent) {
        viewModelScope.launch(Dispatchers.SuitableForIO) {
            _isGenerating.update { true }
            val selection = contentState.selection
            val selectedText = contentState.text.substring(selection)
            val defaultProviderId = dataStoreRepository.getString(
                Constants.Preferences.AI_PROVIDER,
                AI.providers.keys.first()
            )
            val llmProvider = AI.providers[defaultProviderId] ?: AI.providers.values.first()
            val llmConfig = getLLMConfig(llmProvider, dataStoreRepository)
            val response = AI.executePrompt(
                lLMProvider = llmProvider,
                baseUrl = llmConfig.first,
                model = llmConfig.second,
                apiKey = llmConfig.third,
                userInput = when (event) {
                    AIAssistEvent.Rewrite -> AI.EventPrompt.REWRITE + "\n" + selectedText
                    AIAssistEvent.Summarize -> AI.EventPrompt.SUMMARIZE + "\n" + selectedText
                    AIAssistEvent.Elaborate -> AI.EventPrompt.ELABORATE + "\n" + selectedText
                    AIAssistEvent.Proofread -> AI.EventPrompt.PROOFREAD + "\n" + selectedText
                    AIAssistEvent.Shorten -> AI.EventPrompt.SHORTEN + "\n" + selectedText
                    is AIAssistEvent.Generate -> event.prompt
                },
                systemPrompt = when (_templateEditingState.value.noteType) {
                    NoteType.PLAIN_TEXT -> AI.SystemPrompt.PLAIN_TEXT
                    NoteType.MARKDOWN -> AI.SystemPrompt.MARKDOWN
                    NoteType.TODO -> AI.SystemPrompt.TODO_TXT
                    else -> ""
                }
            )
            if (response is GenerationResult.Success) {
                contentState.edit { replace(selection.min, selection.max, response.text) }
            }
            _isGenerating.update { false }
        }
    }
}