package org.yangdai.kori.presentation.screen.note

import ai.koog.agents.utils.SuitableForIO
import ai.koog.prompt.llm.LLMProvider
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
import knet.ai.providers.LMStudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.presentation.component.note.AIAssistEvent
import org.yangdai.kori.presentation.component.note.ProcessedContent
import org.yangdai.kori.presentation.component.note.processMarkdown
import org.yangdai.kori.presentation.component.note.processTodo
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.screen.settings.TemplatePaneState
import org.yangdai.kori.presentation.util.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalFoundationApi::class, ExperimentalUuidApi::class)
class NoteViewModel(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val dataStoreRepository: DataStoreRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Screen.Note>()

    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    private val contentSnapshotFlow = snapshotFlow { contentState.text }

    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    private val _noteEditingState = MutableStateFlow(NoteEditingState())
    val editingState = _noteEditingState.asStateFlow()
    private var oNote = NoteEntity()
    private val _isInitialized = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            if (route.id.isEmpty()) {
                titleState.setTextAndPlaceCursorAtEnd(route.sharedContentTitle)
                contentState.setTextAndPlaceCursorAtEnd(route.sharedContentText)
                val currentTime = Clock.System.now().toString()
                _noteEditingState.update {
                    it.copy(
                        id = Uuid.random().toString(),
                        folderId = route.folderId,
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        noteType = NoteType.entries[route.noteType]
                    )
                }
                oNote = NoteEntity()
            } else {
                noteRepository.getNoteById(route.id)?.let { note ->
                    titleState.setTextAndPlaceCursorAtEnd(note.title)
                    contentState.setTextAndPlaceCursorAtEnd(note.content)
                    _noteEditingState.update {
                        it.copy(
                            id = note.id,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            isPinned = note.isPinned,
                            isDeleted = note.isDeleted,
                            folderId = note.folderId,
                            noteType = note.noteType,
                            isTemplate = note.isTemplate
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

    val formatterState = combine(
        dataStoreRepository.stringFlow(Constants.Preferences.DATE_FORMATTER),
        dataStoreRepository.stringFlow(Constants.Preferences.TIME_FORMATTER)
    ) { dateFormatter, timeFormatter ->
        TemplatePaneState(
            dateFormatter = dateFormatter,
            timeFormatter = timeFormatter
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), TemplatePaneState())

    val templates = noteRepository.getAllTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val editorState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_MARKDOWN_LINT_ENABLED),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_READING_VIEW)
    ) { showLineNumber, isMarkdownLintEnabled, isDefaultReadingView ->
        EditorPaneState(
            showLineNumber = showLineNumber,
            isMarkdownLintEnabled = isMarkdownLintEnabled,
            isDefaultReadingView = isDefaultReadingView
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), EditorPaneState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val foldersWithNoteCounts: StateFlow<List<FolderDao.FolderWithNoteCount>> = dataStoreRepository
        .intFlow(Constants.Preferences.FOLDER_SORT_TYPE)
        .map { FolderSortType.Companion.fromValue(it) }
        .distinctUntilChanged()
        .flatMapLatest { sortType ->
            folderRepository.getFoldersWithNoteCounts(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val processedContent = _isInitialized.filter { it }
        .flatMapLatest {
            snapshotFlow { _noteEditingState.value.noteType }
                .flatMapLatest { noteType ->
                    when (noteType) {
                        NoteType.MARKDOWN -> contentSnapshotFlow.debounce(100)
                            .map { content ->
                                val processed = processMarkdown(content.toString())
                                ProcessedContent.Markdown(processed)
                            }

                        NoteType.TODO -> contentSnapshotFlow.debounce(100)
                            .map { content ->
                                val (undone, done) = processTodo(content.lines())
                                ProcessedContent.Todo(undone, done)
                            }

                        else -> flowOf(ProcessedContent.Empty)
                    }
                }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProcessedContent.Empty
        )

    fun saveOrUpdateNote() {
        if (_noteEditingState.value.isDeleted) return
        viewModelScope.launch {
            val newNote = NoteEntity(
                id = _noteEditingState.value.id,
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                folderId = _noteEditingState.value.folderId,
                createdAt = _noteEditingState.value.createdAt,
                updatedAt = Clock.System.now().toString(),
                isPinned = _noteEditingState.value.isPinned,
                noteType = _noteEditingState.value.noteType
            )
            if (oNote.id.isEmpty()) {
                if (newNote.title.isNotBlank() || newNote.content.isNotBlank()) {
                    noteRepository.insertNote(newNote)
                    oNote = newNote
                }
            } else {
                if (oNote.title != newNote.title || oNote.content != newNote.content ||
                    oNote.folderId != newNote.folderId || oNote.noteType != newNote.noteType ||
                    oNote.isPinned != newNote.isPinned
                ) {
                    noteRepository.updateNote(newNote)
                    oNote = newNote
                }
            }
        }
    }

    fun moveNoteToTrash() {
        viewModelScope.launch {
            _noteEditingState.update { it.copy(isDeleted = true) }
            if (oNote.id.isNotEmpty()) {
                noteRepository.updateNote(
                    NoteEntity(
                        id = _noteEditingState.value.id,
                        title = titleState.text.toString(),
                        content = contentState.text.toString(),
                        folderId = _noteEditingState.value.folderId,
                        createdAt = _noteEditingState.value.createdAt,
                        updatedAt = Clock.System.now().toString(),
                        isDeleted = true,
                        isTemplate = false,
                        isPinned = false,
                        noteType = _noteEditingState.value.noteType
                    )
                )
            }
            _uiEventChannel.send(UiEvent.NavigateUp)
        }
    }

    fun moveNoteToFolder(folderId: String?) {
        _noteEditingState.update {
            it.copy(folderId = folderId)
        }
    }

    fun toggleNotePin() {
        _noteEditingState.update {
            it.copy(isPinned = !it.isPinned)
        }
    }

    fun updateNoteType(noteType: NoteType) {
        _noteEditingState.update {
            it.copy(noteType = noteType)
        }
    }

    fun saveNoteAsTemplate() {
        viewModelScope.launch {
            val currentTime = Clock.System.now().toString()
            val noteEntity = NoteEntity(
                id = Uuid.random().toString(),
                title = titleState.text.toString()
                    .ifBlank { "Template_${Clock.System.now().toEpochMilliseconds()}" },
                content = contentState.text.toString(),
                folderId = null,
                createdAt = currentTime,
                updatedAt = currentTime,
                isDeleted = false,
                isTemplate = true,
                isPinned = false,
                noteType = _noteEditingState.value.noteType
            )
            noteRepository.insertNote(noteEntity)
        }
    }

    /*----*/

    val showAI = combine(
        connectivityObserver.observe(),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_AI_ENABLED),
        snapshotFlow { _noteEditingState.value.noteType }
    ) { status, isAiEnabled, noteType ->
        status == ConnectivityObserver.Status.Connected && isAiEnabled && noteType != NoteType.Drawing
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), false)

    // LLM Config: Base URL, Model, API Key
    private suspend fun getLLMConfig(llmProvider: LLMProvider): Triple<String, String, String> {
        return withContext(Dispatchers.IO) {
            when (llmProvider) {
                LLMProvider.Google -> {
                    val baseUrl =
                        dataStoreRepository.getString(Constants.Preferences.GEMINI_BASE_URL, "")
                    val model =
                        dataStoreRepository.getString(Constants.Preferences.GEMINI_MODEL, "")
                    val apiKey =
                        dataStoreRepository.getString(Constants.Preferences.GEMINI_API_KEY, "")
                    Triple(baseUrl, model, apiKey)
                }

                LLMProvider.OpenAI -> {
                    val baseUrl =
                        dataStoreRepository.getString(Constants.Preferences.OPENAI_BASE_URL, "")
                    val model =
                        dataStoreRepository.getString(Constants.Preferences.OPENAI_MODEL, "")
                    val apiKey =
                        dataStoreRepository.getString(Constants.Preferences.OPENAI_API_KEY, "")
                    Triple(baseUrl, model, apiKey)
                }

                LLMProvider.Anthropic -> {
                    val baseUrl =
                        dataStoreRepository.getString(Constants.Preferences.ANTHROPIC_BASE_URL, "")
                    val model =
                        dataStoreRepository.getString(Constants.Preferences.ANTHROPIC_MODEL, "")
                    val apiKey =
                        dataStoreRepository.getString(Constants.Preferences.ANTHROPIC_API_KEY, "")
                    Triple(baseUrl, model, apiKey)
                }

                LLMProvider.DeepSeek -> {
                    val baseUrl =
                        dataStoreRepository.getString(Constants.Preferences.DEEPSEEK_BASE_URL, "")
                    val model =
                        dataStoreRepository.getString(Constants.Preferences.DEEPSEEK_MODEL, "")
                    val apiKey =
                        dataStoreRepository.getString(Constants.Preferences.DEEPSEEK_API_KEY, "")
                    Triple(baseUrl, model, apiKey)
                }

                LLMProvider.Ollama -> {
                    val baseUrl =
                        dataStoreRepository.getString(Constants.Preferences.OLLAMA_BASE_URL, "")
                    val model =
                        dataStoreRepository.getString(Constants.Preferences.OLLAMA_MODEL, "")
                    Triple(baseUrl, model, "")
                }

                LMStudio -> {
                    val baseUrl =
                        dataStoreRepository.getString(Constants.Preferences.LM_STUDIO_BASE_URL, "")
                    val model =
                        dataStoreRepository.getString(Constants.Preferences.LM_STUDIO_MODEL, "")
                    Triple(baseUrl, model, "")
                }

                else -> {
                    Triple("", "", "")
                }
            }
        }
    }

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
            val llmConfig = getLLMConfig(llmProvider)
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
                systemPrompt = when (_noteEditingState.value.noteType) {
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