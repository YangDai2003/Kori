package org.yangdai.kori.presentation.screen.template

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
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.html.HtmlGenerator
import kmark.parser.MarkdownParser
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.component.note.AIContextMenuEvent
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.addAfter
import org.yangdai.kori.presentation.component.note.findHeadersRecursive
import org.yangdai.kori.presentation.component.note.markdown.Properties.getPropertiesLineRange
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.note.NoteEditingState
import org.yangdai.kori.presentation.screen.note.TextState
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.util.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalFoundationApi::class, ExperimentalUuidApi::class)
class TemplateViewModel(
    savedStateHandle: SavedStateHandle,
    private val dataStoreRepository: DataStoreRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Screen.Template>()

    private val _noteEditingState = MutableStateFlow(NoteEditingState())
    val noteEditingState = _noteEditingState.asStateFlow()

    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    private val contentSnapshotFlow = snapshotFlow { contentState.text }
    private val flavor = GFMFlavourDescriptor()
    private val parser = MarkdownParser(flavor)
    private var oNote = NoteEntity(isTemplate = true)

    init {
        viewModelScope.launch {
            if (route.id.isEmpty()) {
                val currentTime = Clock.System.now().toString()
                _noteEditingState.update {
                    it.copy(
                        id = Uuid.random().toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        isTemplate = true,
                        noteType = NoteType.entries[route.noteType]
                    )
                }
                oNote = NoteEntity(isTemplate = true)
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
        }
    }

    val editorState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_MARKDOWN_LINT_ENABLED)
    ) { showLineNumber, isMarkdownLintEnabled ->
        EditorPaneState(
            showLineNumber = showLineNumber,
            isMarkdownLintEnabled = isMarkdownLintEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), EditorPaneState())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val textState = contentSnapshotFlow.debounce(100).distinctUntilChanged()
        .mapLatest { TextState.fromText(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = TextState()
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val contentAndTreeFlow = contentSnapshotFlow.debounce(100).distinctUntilChanged()
        .mapLatest { content ->
            val text = content.toString()
            val tree = parser.buildMarkdownTreeFromString(text)
            text to tree
        }
        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val html = contentAndTreeFlow
        .mapLatest { (content, tree) ->
            HtmlGenerator(content, tree, flavor, true).generateHtml()
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ""
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val outline = contentAndTreeFlow.debounce(1000)
        .mapLatest { (content, tree) ->
            val root = HeaderNode("", 0, IntRange.EMPTY)
            if (content.isBlank()) return@mapLatest root
            val propertiesLineRange = content.getPropertiesLineRange()
            try {
                val headerStack = mutableListOf(root)
                findHeadersRecursive(tree, content, headerStack, propertiesLineRange)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            root
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HeaderNode("", 0, IntRange.EMPTY)
        )

    fun updateNoteType(noteType: NoteType) {
        _noteEditingState.update {
            it.copy(noteType = noteType)
        }
    }

    fun saveOrUpdateNote() {
        if (_noteEditingState.value.isDeleted) return
        viewModelScope.launch {
            val newNote = NoteEntity(
                id = _noteEditingState.value.id,
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                folderId = null,
                createdAt = _noteEditingState.value.createdAt,
                updatedAt = Clock.System.now().toString(),
                isDeleted = false,
                isTemplate = true,
                isPinned = false,
                noteType = _noteEditingState.value.noteType
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
            _noteEditingState.update { it.copy(isDeleted = true) }
            if (oNote.id.isNotEmpty()) {
                noteRepository.deleteNoteById(_noteEditingState.value.id)
            }
            _uiEventChannel.send(UiEvent.NavigateUp)
        }
    }

    /*----*/

    val isAIEnabled = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_AI_ENABLED),
        snapshotFlow { _noteEditingState.value.noteType }
    ) { isAiEnabled, noteType -> isAiEnabled && noteType != NoteType.Drawing }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), false)

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

    fun generateNoteFromPrompt(
        userInput: String,
        onSuccess: () -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.SuitableForIO) {
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
                userInput = userInput,
                systemPrompt = when (_noteEditingState.value.noteType) {
                    NoteType.PLAIN_TEXT -> AI.SystemPrompt.PLAIN_TEXT
                    NoteType.MARKDOWN -> AI.SystemPrompt.MARKDOWN
                    NoteType.TODO -> AI.SystemPrompt.TODO_TXT
                    else -> ""
                }
            )
            when (response) {
                is GenerationResult.Error -> {
                    withContext(Dispatchers.Main) {
                        onError(response.errorMessage)
                    }
                }

                is GenerationResult.Success -> {
                    contentState.edit { addAfter(response.text) }
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            }
        }
    }

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    fun onAIContextMenuEvent(event: AIContextMenuEvent) {
        viewModelScope.launch(Dispatchers.SuitableForIO) {
            val selection = contentState.selection
            if (selection.collapsed) return@launch
            _isGenerating.update { true }
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
                    AIContextMenuEvent.Rewrite -> AI.EventPrompt.REWRITE
                    AIContextMenuEvent.Summarize -> AI.EventPrompt.SUMMARIZE
                } + "\n" + selectedText,
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