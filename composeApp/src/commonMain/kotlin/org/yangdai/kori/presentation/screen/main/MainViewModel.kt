package org.yangdai.kori.presentation.screen.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
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
import org.yangdai.kori.presentation.util.SampleMarkdownNote
import org.yangdai.kori.presentation.util.SampleTodoNote
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
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

    private var _currentFolderId by mutableStateOf("")
    private val _currentFolderNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val currentFolderNotes = _currentFolderNotes.asStateFlow()

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
    val trashNotes: StateFlow<Pair<List<NoteEntity>, String>> = _noteSortTypeFlow
        .flatMapLatest { sortType ->
            noteRepository.getNotesInTrash(sortType)
                .map { notes: List<NoteEntity> ->
                    val totalBytes = notes.sumOf { it.content.toByteArray(Charsets.UTF_8).size }
                    val sizeString = when {
                        totalBytes == 0 -> ""
                        totalBytes < 1024 -> "$totalBytes B"
                        totalBytes < 1024 * 1024 -> {
                            val kb = totalBytes / 1024.0
                            "${(round(kb * 100) / 100)} KB"
                        }

                        else -> {
                            val mb = totalBytes / 1024.0 / 1024.0
                            "${(round(mb * 100) / 100)} MB"
                        }
                    }
                    Pair(notes, sizeString)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = Pair(emptyList(), "")
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
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository
                .getNotesByFolderId(folderId, noteSortType)
                .collect { notes -> _currentFolderNotes.value = notes }
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
            val newSet = buildSet {
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

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    fun duplicateNotes(noteIds: Set<String>) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                val note = noteRepository.getNoteById(noteId)
                note?.let {
                    val duplicateNote = it.copy(
                        id = Uuid.random().toString(),
                        title = it.title + " (\uD83D\uDCD1)",
                        createdAt = Clock.System.now().toString(),
                        updatedAt = Clock.System.now().toString()
                    )
                    noteRepository.insertNote(duplicateNote)
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
    suspend fun addSampleNote(noteType: NoteType): String {
        val noteId = Uuid.random().toString()
        noteRepository.insertNote(
            NoteEntity(
                id = noteId,
                title = "Sample Note" + when (noteType) {
                    NoteType.MARKDOWN -> " - Markdown"
                    NoteType.TODO -> " - Todo.txt"
                    else -> ""
                },
                content = when (noteType) {
                    NoteType.MARKDOWN -> SampleMarkdownNote
                    NoteType.TODO -> SampleTodoNote
                    else -> ""
                },
                folderId = null,
                isPinned = true,
                isDeleted = false,
                noteType = noteType
            )
        )
        return noteId
    }
}