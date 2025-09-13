package org.yangdai.kori.presentation.screen.file

import ai.koog.agents.utils.SuitableForIO
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.substring
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kfile.PlatformFile
import kfile.delete
import kfile.getExtension
import kfile.getFileName
import kfile.getLastModified
import kfile.readText
import kfile.writeText
import knet.ConnectivityObserver
import knet.ai.AI
import knet.ai.GenerationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.component.note.AIAssistEvent
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.screen.settings.TemplatePaneState
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.Constants.LLMConfig.getLLMConfig
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
class FileViewModel(
    private val noteRepository: NoteRepository,
    private val dataStoreRepository: DataStoreRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {
    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    private val contentSnapshotFlow = snapshotFlow { contentState.text }
    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()
    private val _fileEditingState = MutableStateFlow(FileEditingState())
    val editingState = _fileEditingState.asStateFlow()
    private val _initialContent = MutableStateFlow("")
    private val _isInitialized = MutableStateFlow(false)
    val needSave = combine(
        _initialContent,
        contentSnapshotFlow
    ) { initial, current -> initial != current.toString() }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    @OptIn(ExperimentalFoundationApi::class)
    fun loadFile(file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val title = file.getFileName()
            val content = file.readText()
            val noteType = if (file.getExtension().lowercase() in listOf(
                    "md",
                    "markdown",
                    "mkd",
                    "mdwn",
                    "mdown",
                    "mdtxt",
                    "mdtext",
                    "html"
                )
            ) NoteType.MARKDOWN
            else if (
                title.contains("todo", ignoreCase = true)
                && file.getExtension().lowercase() == "txt"
            ) NoteType.TODO
            else NoteType.PLAIN_TEXT
            titleState.setTextAndPlaceCursorAtEnd(title)
            contentState.setTextAndPlaceCursorAtEnd(content)
            titleState.undoState.clearHistory()
            contentState.undoState.clearHistory()
            // 记录初始内容
            _initialContent.value = content
            val updatedAt = file.getLastModified().toString()
            _fileEditingState.update {
                it.copy(
                    updatedAt = updatedAt,
                    fileType = noteType
                )
            }
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), TemplatePaneState())

    val templates = noteRepository.getAllTemplates().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )

    val editorState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_LINTING_ENABLED),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_READING_VIEW)
    ) { showLineNumber, isLintingEnabled, isDefaultReadingView ->
        EditorPaneState(
            isLineNumberVisible = showLineNumber,
            isLintingEnabled = isLintingEnabled,
            isDefaultReadingView = isDefaultReadingView
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EditorPaneState())

    fun saveFile(file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = contentState.text.toString()
            file.writeText(content)
            _initialContent.value = content
        }
    }

    fun deleteFile(file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.delete())
                _uiEventChannel.send(UiEvent.NavigateUp)
        }
    }

    fun updateFileType(noteType: NoteType) {
        _fileEditingState.update {
            it.copy(fileType = noteType)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
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
                noteType = _fileEditingState.value.fileType
            )
            noteRepository.insertNote(noteEntity)
        }
    }

    /*----*/

    val showAI = combine(
        connectivityObserver.observe(),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_AI_ENABLED),
        snapshotFlow { _fileEditingState.value.fileType }
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
                systemPrompt = when (_fileEditingState.value.fileType) {
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