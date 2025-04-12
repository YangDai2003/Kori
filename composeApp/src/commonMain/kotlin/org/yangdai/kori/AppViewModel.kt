package org.yangdai.kori

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.NoteSortType
import org.yangdai.kori.domain.sort.SortDirection
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AppViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val titleState = TextFieldState()
    val contentState = TextFieldState()

    // 笔记状态
    private val _activeNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val activeNotes: StateFlow<List<NoteEntity>> = _activeNotes

    private val _trashNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val trashNotes: StateFlow<List<NoteEntity>> = _trashNotes

    private val _currentFolderNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val currentFolderNotes: StateFlow<List<NoteEntity>> = _currentFolderNotes

    private val _searchResults = MutableStateFlow<List<NoteEntity>>(emptyList())
    val searchResults: StateFlow<List<NoteEntity>> = _searchResults

    // 添加 foldersWithNoteCounts
    private val _foldersWithNoteCounts = MutableStateFlow<List<FolderWithNoteCount>>(emptyList())
    val foldersWithNoteCounts: StateFlow<List<FolderWithNoteCount>> = _foldersWithNoteCounts

    // 计数
    val activeNotesCount = noteRepository.getActiveNotesCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val trashNotesCount = noteRepository.getTrashNotesCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val templateNotesCount = noteRepository.getTemplatesCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // 排序相关
    var noteSortType by mutableStateOf(NoteSortType.UPDATED)
        private set

    var noteSortDirection by mutableStateOf(SortDirection.DESC)
        private set

    var folderSortType by mutableStateOf(FolderSortType.NAME)
        private set

    var folderSortDirection by mutableStateOf(SortDirection.ASC)
        private set

    private var _currentFolderId = MutableStateFlow<String?>(null)
    val currentFolderId: StateFlow<String?> = _currentFolderId

    init {
        loadAllNotes()
        loadTrashNotes()
        loadFoldersWithNoteCounts() // 添加加载文件夹及笔记数量
    }

    // 笔记相关方法
    fun loadAllNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes(noteSortType, noteSortDirection)
                .collect { notes ->
                    _activeNotes.value = notes
                }
        }
    }

    fun loadTrashNotes() {
        viewModelScope.launch {
            noteRepository.getNotesInTrash(noteSortType, noteSortDirection)
                .collect { notes ->
                    _trashNotes.value = notes
                }
        }
    }

    fun loadNotesByFolder(folderId: String) {
        viewModelScope.launch {
            _currentFolderId.value = folderId
            noteRepository.getNotesByFolderId(folderId, noteSortType, noteSortDirection)
                .collect { notes ->
                    _currentFolderNotes.value = notes
                }
        }
    }

    fun searchNotes(keyword: String) {
        viewModelScope.launch {
            noteRepository.searchNotesByKeyword(keyword, noteSortType, noteSortDirection)
                .collect { notes ->
                    _searchResults.value = notes
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createNote(title: String, content: String, folderId: String? = null) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = Uuid.random().toString(),
                title = title,
                content = content,
                folderId = folderId,
                createdAt = Clock.System.now().toString(),
                updatedAt = Clock.System.now().toString()
            )
            noteRepository.insertNote(note)
            loadAllNotes()
            folderId?.let { loadNotesByFolder(it) }
        }
    }

    fun updateNote(noteId: String, title: String, content: String, folderId: String? = null) {
        viewModelScope.launch {
            val existingNote = noteRepository.getNoteById(noteId)
            existingNote?.let {
                val updatedNote = it.copy(
                    title = title,
                    content = content,
                    folderId = folderId,
                    updatedAt = Clock.System.now().toString()
                )
                noteRepository.updateNote(updatedNote)
                loadAllNotes()
                it.folderId?.let { oldFolderId -> loadNotesByFolder(oldFolderId) }
                folderId?.let { newFolderId ->
                    if (newFolderId != it.folderId) loadNotesByFolder(newFolderId)
                }
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(noteId)
            loadAllNotes()
            _currentFolderId.value?.let { loadNotesByFolder(it) }
        }
    }

    fun moveNoteToTrash(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            note?.let {
                val trashNote = it.copy(isDeleted = true, updatedAt = Clock.System.now().toString())
                noteRepository.updateNote(trashNote)
                loadAllNotes()
                it.folderId?.let { folderId -> loadNotesByFolder(folderId) }
                loadTrashNotes()
            }
        }
    }

    fun restoreNoteFromTrash(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            note?.let {
                val restoredNote =
                    it.copy(isDeleted = false, updatedAt = Clock.System.now().toString())
                noteRepository.updateNote(restoredNote)
                loadAllNotes()
                it.folderId?.let { folderId -> loadNotesByFolder(folderId) }
                loadTrashNotes()
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
            loadTrashNotes()
        }
    }

    fun restoreAllFromTrash() {
        viewModelScope.launch {
            noteRepository.restoreAllFromTrash()
            loadAllNotes()
            loadTrashNotes()
            _currentFolderId.value?.let { loadNotesByFolder(it) }
        }
    }

    fun getNoteById(noteId: String?) {
        if (noteId == null) {
            titleState.clearText()
            contentState.clearText()
            return
        }

        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            note?.let {
                titleState.setTextAndPlaceCursorAtEnd(it.title)
                contentState.setTextAndPlaceCursorAtEnd(it.content)
            }
        }
    }

    // 添加加载文件夹及笔记数量的方法
    fun loadFoldersWithNoteCounts() {
        viewModelScope.launch {
            folderRepository.getFoldersWithNoteCounts(folderSortType, folderSortDirection)
                .collect { foldersList ->
                    _foldersWithNoteCounts.value = foldersList
                }
        }
    }

    fun createFolder(folderEntity: FolderEntity) {
        viewModelScope.launch {
            folderRepository.insertFolder(folderEntity)
        }
    }

    fun updateFolder(
        folderId: String,
        name: String,
        colorValue: Long = FolderEntity.defaultColorValue
    ) {
        viewModelScope.launch {
            val folder = FolderEntity(
                id = folderId,
                name = name,
                colorValue = colorValue
            )
            folderRepository.updateFolder(folder)
            loadFoldersWithNoteCounts() // 更新文件夹及笔记数量
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            folderRepository.deleteFolderById(folderId)
            if (_currentFolderId.value == folderId) {
                _currentFolderId.value = null
                _currentFolderNotes.value = emptyList()
            }
        }
    }

    // 排序设置
    fun setNoteSorting(sortType: NoteSortType, direction: SortDirection) {
        noteSortType = sortType
        noteSortDirection = direction
        loadAllNotes()
        loadTrashNotes()
        _currentFolderId.value?.let { loadNotesByFolder(it) }
    }

    fun setFolderSorting(sortType: FolderSortType, direction: SortDirection) {
        folderSortType = sortType
        folderSortDirection = direction
        loadFoldersWithNoteCounts() // 更新文件夹及笔记数量
    }
}
