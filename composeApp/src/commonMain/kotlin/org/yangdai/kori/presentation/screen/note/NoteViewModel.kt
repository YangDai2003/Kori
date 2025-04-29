package org.yangdai.kori.presentation.screen.note

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kori.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.event.UiEvent
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.screen.settings.TemplatePaneState
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.Properties.getPropertiesLineRange
import org.yangdai.kori.presentation.util.Properties.splitPropertiesAndContent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class NoteViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    val contentSnapshotFlow = snapshotFlow { contentState.text }

    val appTheme = dataStoreRepository.intFlow(Constants.Preferences.APP_THEME)
        .map { AppTheme.fromInt(it) }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), AppTheme.SYSTEM)

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

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val textState = contentSnapshotFlow.debounce(100).distinctUntilChanged()
        .mapLatest { TextState.fromText(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = TextState()
        )

    private val _noteEditingState = MutableStateFlow<NoteEditingState>(NoteEditingState())
    val noteEditingState: StateFlow<NoteEditingState> = _noteEditingState.asStateFlow()

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
    val outline = contentSnapshotFlow.debounce(1000)
        .mapLatest {
            val text = it.toString()
            val root = HeaderNode("", 0, IntRange.EMPTY)
            if (text.isBlank()) return@mapLatest root
            val propertiesLineRange = text.getPropertiesLineRange()
            val headerStack = mutableListOf(root)

            root
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HeaderNode("", 0, IntRange.EMPTY)
        )

    var baseHtml: String? = null

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val html = contentSnapshotFlow.debounce(100)
        .mapLatest {
            var content = it.toString()
            content = content.splitPropertiesAndContent().second
            baseHtml?.replace("{{CONTENT}}", content) ?: ""
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ""
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            baseHtml = Res.readBytes("files/template.html").decodeToString()
        }
    }

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow: SharedFlow<UiEvent> = _uiEventFlow.asSharedFlow()

    var oNote = NoteEntity()

    fun loadNoteById(id: String, folderId: String?) {
        viewModelScope.launch {
            if (id.isEmpty()) {
                val currentTime = Clock.System.now().toString()
                _noteEditingState.update {
                    it.copy(
                        id = id,
                        folderId = folderId,
                        createdAt = currentTime,
                        updatedAt = currentTime,
                    )
                }
                oNote = NoteEntity()
            } else
                noteRepository.getNoteById(id)?.let { note ->
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

    @OptIn(ExperimentalUuidApi::class)
    fun saveOrUpdateNote() {
        if (_noteEditingState.value.isDeleted) return
        viewModelScope.launch {
            val noteEntity = NoteEntity(
                id = _noteEditingState.value.id,
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                folderId = _noteEditingState.value.folderId,
                createdAt = _noteEditingState.value.createdAt,
                updatedAt = Clock.System.now().toString(),
                isDeleted = false,
                isTemplate = false,
                isPinned = _noteEditingState.value.isPinned,
                noteType = _noteEditingState.value.noteType
            )
            if (noteEntity.id.isEmpty() && (noteEntity.title.isNotBlank() || noteEntity.content.isNotBlank())) {
                noteRepository.insertNote(noteEntity.copy(id = Uuid.random().toString()))
            } else {
                if (oNote.title != noteEntity.title || oNote.content != noteEntity.content ||
                    oNote.folderId != noteEntity.folderId || oNote.isTemplate != noteEntity.isTemplate ||
                    oNote.isPinned != noteEntity.isPinned || oNote.noteType != noteEntity.noteType
                )
                    noteRepository.updateNote(noteEntity)
            }
        }
    }

    fun moveNoteToTrash() {
        viewModelScope.launch {
            _noteEditingState.update { it.copy(isDeleted = true) }
            if (_noteEditingState.value.id.isNotEmpty()) {
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
            _uiEventFlow.emit(UiEvent.NavigateUp)
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

    @OptIn(ExperimentalUuidApi::class)
    fun saveNoteAsTemplate() {
        viewModelScope.launch {
            val currentTime = Clock.System.now().toString()
            val noteEntity = NoteEntity(
                id = Uuid.random().toString(),
                title = titleState.text.toString()
                    .ifBlank {
                        "Template " + Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(LocalDateTime.Formats.ISO)
                    },
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
}