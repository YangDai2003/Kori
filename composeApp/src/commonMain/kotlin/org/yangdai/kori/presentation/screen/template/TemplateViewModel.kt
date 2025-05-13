package org.yangdai.kori.presentation.screen.template

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.html.HtmlGenerator
import kmark.parser.MarkdownParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.note.NoteEditingState
import org.yangdai.kori.presentation.screen.note.TextState
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.util.Constants
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TemplateViewModel(
    savedStateHandle: SavedStateHandle,
    dataStoreRepository: DataStoreRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Screen.Template>()

    private val _noteEditingState = MutableStateFlow(NoteEditingState())
    val noteEditingState = _noteEditingState.asStateFlow()

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    val contentSnapshotFlow = snapshotFlow { contentState.text }
    val flavor = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavor)
    var oNote = NoteEntity(isTemplate = true)

    init {
        viewModelScope.launch {
            if (route.id.isEmpty()) {
                val currentTime = Clock.System.now().toString()
                _noteEditingState.update {
                    it.copy(
                        id = route.id,
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        isTemplate = true
                    )
                }
            } else
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

    val appTheme = dataStoreRepository.intFlow(Constants.Preferences.APP_THEME)
        .map { AppTheme.fromInt(it) }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), AppTheme.SYSTEM)

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

    fun updateNoteType(noteType: NoteType) {
        _noteEditingState.update {
            it.copy(noteType = noteType)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveOrUpdateNote() {
        if (_noteEditingState.value.isDeleted) return
        viewModelScope.launch {
            val noteEntity = NoteEntity(
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
            if (noteEntity.id.isEmpty() && (noteEntity.title.isNotBlank() || noteEntity.content.isNotBlank())) {
                val id = Uuid.random().toString()
                noteRepository.insertNote(noteEntity.copy(id = id))
                _noteEditingState.update { it.copy(id = id) }
            } else {
                if (oNote.title != noteEntity.title || oNote.content != noteEntity.content ||
                    oNote.noteType != noteEntity.noteType
                ) noteRepository.updateNote(noteEntity)
            }
        }
    }

    fun deleteTemplate() {
        viewModelScope.launch {
            _noteEditingState.update { it.copy(isDeleted = true) }
            if (_noteEditingState.value.id.isNotEmpty()) {
                noteRepository.deleteNoteById(_noteEditingState.value.id)
            }
            _uiEventFlow.emit(UiEvent.NavigateUp)
        }
    }
}