package org.yangdai.kori.presentation.screen.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.NoteSortType
import org.yangdai.kori.presentation.screen.settings.CardPaneState
import org.yangdai.kori.presentation.screen.settings.CardSize
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.SampleNote
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MainViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val isAppProtected: StateFlow<Boolean> = dataStoreRepository
        .stringFlow(Constants.Preferences.PASSWORD)
        .map { password -> password.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, false)

    private var _currentFolderId by mutableStateOf<String>("")
    private val _currentFolderNotesMap =
        MutableStateFlow<Map<Boolean, List<NoteEntity>>>(emptyMap())
    val currentFolderNotesMap = _currentFolderNotesMap.asStateFlow()

    private val _searchResults = MutableStateFlow<List<NoteEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    val searchHistorySet: StateFlow<Set<String>> = dataStoreRepository
        .stringSetFlow(Constants.Preferences.SEARCH_HISTORY)
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), emptySet())

    // 计数
    val activeNotesCount = noteRepository.getActiveNotesCount()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), 0)

    val trashNotesCount = noteRepository.getTrashNotesCount()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), 0)

    val templateNotesCount = noteRepository.getTemplatesCount()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), 0)

    // 排序相关
    var noteSortType by mutableStateOf(NoteSortType.UPDATE_TIME_DESC)
        private set

    private val _noteSortTypeFlow = dataStoreRepository
        .intFlow(Constants.Preferences.NOTE_SORT_TYPE)
        .map { NoteSortType.Companion.fromValue(it).also { sortType -> noteSortType = sortType } }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allNotes: StateFlow<List<NoteEntity>> = _noteSortTypeFlow
        .flatMapLatest { sortType ->
            noteRepository.getAllNotes(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val templateNotes: StateFlow<List<NoteEntity>> = _noteSortTypeFlow
        .flatMapLatest { sortType ->
            noteRepository.getAllTemplates(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val trashNotes: StateFlow<List<NoteEntity>> = _noteSortTypeFlow
        .flatMapLatest { sortType ->
            noteRepository.getNotesInTrash(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    var folderSortType by mutableStateOf(FolderSortType.CREATE_TIME_DESC)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val foldersWithNoteCounts: StateFlow<List<FolderDao.FolderWithNoteCount>> = dataStoreRepository
        .intFlow(Constants.Preferences.FOLDER_SORT_TYPE)
        .map {
            FolderSortType.Companion.fromValue(it).also { sortType -> folderSortType = sortType }
        }
        .distinctUntilChanged()
        .flatMapLatest { sortType ->
            folderRepository.getFoldersWithNoteCounts(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val cardPaneState = combine(
        dataStoreRepository.intFlow(Constants.Preferences.CARD_SIZE),
        dataStoreRepository.booleanFlow(Constants.Preferences.CLIP_OVERFLOW_TEXT)
    ) { cardSize, clipOverflow ->
        CardPaneState(
            cardSize = CardSize.fromInt(cardSize),
            clipOverflow = clipOverflow
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), CardPaneState())

    fun loadNotesByFolder(folderId: String) {
        _currentFolderId = folderId
        viewModelScope.launch {
            noteRepository
                .getNotesByFolderId(folderId, noteSortType)
                .map { notes -> notes.groupBy { it.isPinned } }
                .flowOn(Dispatchers.Default)
                .collect { notesMap ->
                    _currentFolderNotesMap.value = notesMap
                }
        }
    }

    fun searchNotes(keyword: String) {
        if (keyword.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val currentSet = searchHistorySet.value
            // 创建一个新集合，首先添加新关键词，然后添加旧的关键词，但总数不超过30
            val newSet = buildSet<String> {
                add(keyword)  // 确保新关键词在最前面
                addAll(currentSet.filter { it != keyword }.take(29))  // 过滤掉相同的关键词，并限制只取29个
            }
            dataStoreRepository.putStringSet(Constants.Preferences.SEARCH_HISTORY, newSet)
            noteRepository.searchNotesByKeyword(keyword, noteSortType)
                .collect { notes ->
                    _searchResults.value = notes
                }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            dataStoreRepository.putStringSet(Constants.Preferences.SEARCH_HISTORY, emptySet())
        }
    }

    fun deleteNotes(noteIds: Set<String>) {
        viewModelScope.launch {
            noteRepository.deleteNoteByIds(noteIds.toList())
        }
    }

    fun pinNotes(noteIds: Set<String>) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                val note = noteRepository.getNoteById(noteId)
                note?.let {
                    val updatedNote = it.copy(isPinned = true)
                    noteRepository.updateNote(updatedNote)
                }
            }
        }
    }

    fun moveNotesToFolder(noteIds: Set<String>, folderId: String?) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                val note = noteRepository.getNoteById(noteId)
                note?.let {
                    val updatedNote = it.copy(folderId = folderId)
                    noteRepository.updateNote(updatedNote)
                }
            }
        }
    }

    fun moveNotesToTrash(noteIds: Set<String>) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                val note = noteRepository.getNoteById(noteId)
                note?.let {
                    val trashNote = it.copy(isDeleted = true, isPinned = false)
                    noteRepository.updateNote(trashNote)
                }
            }
        }
    }

    fun restoreNotesFromTrash(noteIds: Set<String>) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                val note = noteRepository.getNoteById(noteId)
                note?.let {
                    val restoredNote = it.copy(isDeleted = false)
                    noteRepository.updateNote(restoredNote)
                }
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
        }
    }

    fun restoreAllNotesFromTrash() {
        viewModelScope.launch {
            noteRepository.restoreAllFromTrash()
        }
    }

    // 笔记排序设置
    fun setNoteSorting(sortType: NoteSortType) {
        viewModelScope.launch {
            dataStoreRepository.putInt(Constants.Preferences.NOTE_SORT_TYPE, sortType.value)
            noteSortType = sortType
            loadNotesByFolder(_currentFolderId)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addSampleNote(folderId: String? = null) {
        viewModelScope.launch {
            noteRepository.insertNote(
                NoteEntity(
                    id = Uuid.random().toString(),
                    title = "Sample Note",
                    content = SampleNote,
                    folderId = folderId,
                    isPinned = true,
                    isDeleted = false,
                    noteType = NoteType.MARKDOWN
                )
            )
        }
    }
}