package org.yangdai.kori.presentation.viewModel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.presentation.event.UiEvent
import org.yangdai.kori.presentation.state.NoteEditingState
import org.yangdai.kori.presentation.util.Constants
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

    private val _noteEditingState = MutableStateFlow(NoteEditingState())
    val noteEditingState: StateFlow<NoteEditingState> = _noteEditingState

    @OptIn(ExperimentalCoroutinesApi::class)
    val foldersWithNoteCounts: StateFlow<List<FolderDao.FolderWithNoteCount>> = dataStoreRepository
        .intFlow(Constants.Preferences.FOLDER_SORT_TYPE)
        .flowOn(Dispatchers.IO)
        .map { FolderSortType.fromValue(it) }
        .distinctUntilChanged()
        .flatMapLatest { sortType ->
            folderRepository.getFoldersWithNoteCounts(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow: SharedFlow<UiEvent> = _uiEventFlow

    var oNote = NoteEntity()

    fun loadNoteById(id: String) {
        viewModelScope.launch {
            if (id.isEmpty()) {
                titleState.setTextAndPlaceCursorAtEnd("")
                contentState.setTextAndPlaceCursorAtEnd("")
                _noteEditingState.value = NoteEditingState(
                    id = id,
                    createdAt = Clock.System.now().toString(),
                    updatedAt = Clock.System.now().toString()
                )
                oNote = NoteEntity()
                return@launch
            }
            noteRepository.getNoteById(id)?.let { note ->
                titleState.setTextAndPlaceCursorAtEnd(note.title)
                contentState.setTextAndPlaceCursorAtEnd(note.content)
                _noteEditingState.value = NoteEditingState(
                    id = note.id,
                    folderId = note.folderId,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                    isDeleted = note.isDeleted,
                    isTemplate = note.isTemplate,
                    isPinned = note.isPinned,
                    noteType = note.noteType
                )
                oNote = note
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveOrUpdateNote() {
        if (_noteEditingState.value.isDeleted) {
            return
        }
        viewModelScope.launch {
            val noteEntity = NoteEntity(
                id = _noteEditingState.value.id,
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                folderId = _noteEditingState.value.folderId,
                createdAt = _noteEditingState.value.createdAt,
                updatedAt = Clock.System.now().toString(),
                isDeleted = _noteEditingState.value.isDeleted,
                isTemplate = _noteEditingState.value.isTemplate,
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
            if (_noteEditingState.value.id.isNotEmpty()) {
                _noteEditingState.update {
                    it.copy(isDeleted = true)
                }
                noteRepository.updateNote(
                    NoteEntity(
                        id = _noteEditingState.value.id,
                        title = titleState.text.toString(),
                        content = contentState.text.toString(),
                        folderId = _noteEditingState.value.folderId,
                        createdAt = _noteEditingState.value.createdAt,
                        updatedAt = Clock.System.now().toString(),
                        isDeleted = true,
                        isTemplate = _noteEditingState.value.isTemplate,
                        isPinned = _noteEditingState.value.isPinned,
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
}
