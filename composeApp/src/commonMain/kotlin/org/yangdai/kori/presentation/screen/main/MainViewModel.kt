package org.yangdai.kori.presentation.screen.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kfile.PlatformFile
import kfile.getExtension
import kfile.getFileName
import kfile.getLastModified
import kfile.readText
import knet.ai.AI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.data.local.entity.defaultFolderColor
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.NoteSortType
import org.yangdai.kori.presentation.screen.settings.AiPaneState
import org.yangdai.kori.presentation.screen.settings.AppColor
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.screen.settings.BackupData
import org.yangdai.kori.presentation.screen.settings.CardPaneState
import org.yangdai.kori.presentation.screen.settings.CardSize
import org.yangdai.kori.presentation.screen.settings.DataActionState
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.screen.settings.OpenNoteBackupData
import org.yangdai.kori.presentation.screen.settings.SecurityPaneState
import org.yangdai.kori.presentation.screen.settings.StylePaneState
import org.yangdai.kori.presentation.screen.settings.TemplatePaneState
import org.yangdai.kori.presentation.screen.settings.decryptBackupDataWithCompatibility
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.SampleMarkdownNote
import org.yangdai.kori.presentation.util.SampleTodoNote
import kotlin.io.encoding.Base64
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
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

    /*----*/

    val stylePaneState = combine(
        dataStoreRepository.intFlow(Constants.Preferences.APP_THEME),
        dataStoreRepository.intFlow(Constants.Preferences.APP_COLOR),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_AMOLED_MODE),
        dataStoreRepository.floatFlow(Constants.Preferences.FONT_SIZE)
    ) { theme, color, isAppInAmoledMode, fontSize ->
        StylePaneState(
            theme = AppTheme.fromInt(theme),
            color = AppColor.fromInt(color),
            isAppInAmoledMode = isAppInAmoledMode,
            fontSize = fontSize
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), StylePaneState())

    val securityPaneState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_SCREEN_PROTECTED),
        dataStoreRepository.stringFlow(Constants.Preferences.PASSWORD),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_CREATING_PASSWORD),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_BIOMETRIC_ENABLED),
        dataStoreRepository.booleanFlow(Constants.Preferences.KEEP_SCREEN_ON)
    ) { isScreenProtected, password, isCreatingPass, isBiometricEnabled, keepScreenOn ->
        SecurityPaneState(
            isScreenProtected = isScreenProtected,
            password = password,
            isCreatingPass = isCreatingPass,
            isBiometricEnabled = isBiometricEnabled,
            keepScreenOn = keepScreenOn
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), SecurityPaneState())

    val editorPaneState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_LINTING_ENABLED),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_READING_VIEW)
    ) { showLineNumber, isLintingEnabled, isDefaultReadingView ->
        EditorPaneState(
            isLineNumberVisible = showLineNumber,
            isLintingEnabled = isLintingEnabled,
            isDefaultReadingView = isDefaultReadingView
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), EditorPaneState())

    val templatePaneState = combine(
        dataStoreRepository.stringFlow(Constants.Preferences.DATE_FORMATTER),
        dataStoreRepository.stringFlow(Constants.Preferences.TIME_FORMATTER)
    ) { dateFormatter, timeFormatter ->
        TemplatePaneState(
            dateFormatter = dateFormatter,
            timeFormatter = timeFormatter
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), TemplatePaneState())

    val cardPaneState = combine(
        dataStoreRepository.intFlow(Constants.Preferences.CARD_SIZE),
        dataStoreRepository.booleanFlow(Constants.Preferences.CLIP_OVERFLOW_TEXT)
    ) { cardSize, clipOverflow ->
        CardPaneState(
            cardSize = CardSize.fromInt(cardSize),
            clipOverflow = clipOverflow
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), CardPaneState())

    val aiPaneState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_AI_ENABLED),
        dataStoreRepository.stringFlow(Constants.Preferences.AI_PROVIDER)
    ) { isAiEnabled, aiProvider ->
        AiPaneState(
            isAiEnabled = isAiEnabled,
            llmProvider = AI.providers[aiProvider] ?: AI.providers.values.first()
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), AiPaneState())

    fun getStringValue(key: String): String =
        dataStoreRepository.getString(key, "")

    fun getFloatValue(key: String): Float =
        dataStoreRepository.getFloat(key, 1f)

    fun <T> putPreferenceValue(key: String, value: T) {
        viewModelScope.launch {
            when (value) {
                is Int -> dataStoreRepository.putInt(key, value)
                is Float -> dataStoreRepository.putFloat(key, value)
                is Boolean -> dataStoreRepository.putBoolean(key, value)
                is String -> dataStoreRepository.putString(key, value)
                is Set<*> -> dataStoreRepository.putStringSet(
                    key, value.filterIsInstance<String>().toSet()
                )

                else -> throw IllegalArgumentException("Unsupported value type")
            }
        }
    }

    /*----*/

    private val _dataActionState = MutableStateFlow(DataActionState())
    val dataActionState = _dataActionState.asStateFlow()
    private var dataActionJob: Job? = null

    fun cancelDataAction() {
        dataActionJob?.cancel()
        _dataActionState.value = DataActionState()
    }

    fun resetDatabase() {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch(Dispatchers.IO) {
            _dataActionState.value = DataActionState(infinite = true, progress = 0f)
            delay(300L) // 等待进度弹窗出现
            runCatching {
                noteRepository.deleteAllNotes()
                folderRepository.deleteAllFolders()
            }.onSuccess {
                _dataActionState.update { it.copy(progress = 1f) }
                delay(1500L)
                _dataActionState.value = DataActionState()
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    fun importFiles(files: List<PlatformFile>, folderId: String?) {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch(Dispatchers.IO) {
            _dataActionState.value = DataActionState(progress = 0f)
            delay(300L)
            runCatching {
                files.forEachIndexed { index, it ->
                    val title = it.getFileName()
                    val content = it.readText()
                    val modified = it.getLastModified().toString()
                    val noteType = if (it.getExtension().lowercase() in listOf(
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
                        && it.getExtension().lowercase() == "txt"
                    ) NoteType.TODO
                    else NoteType.PLAIN_TEXT
                    val noteEntity = NoteEntity(
                        id = Uuid.random().toString(),
                        title = title,
                        content = content,
                        createdAt = modified,
                        updatedAt = modified,
                        folderId = folderId,
                        noteType = noteType
                    )
                    noteRepository.insertNote(noteEntity)
                    _dataActionState.update { it.copy(progress = (index + 1) / files.size.toFloat()) }
                }
                _dataActionState.update { it.copy(progress = 1f) }
            }.onSuccess {
                delay(3000L)
                _dataActionState.value = DataActionState()
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }

    fun createBackupJson(onCreated: (String) -> Unit) {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch(Dispatchers.IO) {
            _dataActionState.value = DataActionState(infinite = true, progress = 0f)
            delay(300L)
            runCatching {
                val notes = (noteRepository.getAllNotes().firstOrNull() ?: emptyList()).map {
                    it.copy(
                        title = Base64.Default.encode(it.title.encodeToByteArray()),
                        content = Base64.Default.encode(it.content.encodeToByteArray())
                    )
                }
                val folders = folderRepository.getFoldersWithNoteCounts()
                    .firstOrNull()
                    ?.map { it.folder }
                    ?: emptyList()
                val backupData = BackupData(notes, folders)
                val jsonString = Json.encodeToString(backupData)
                _dataActionState.update { it.copy(progress = 1f) }
                delay(300L)
                withContext(Dispatchers.Main) {
                    onCreated(jsonString)
                }
            }.onSuccess {
                delay(1500L)
                _dataActionState.value = DataActionState()
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }

    fun restoreFromJson(json: String) {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch(Dispatchers.IO) {
            _dataActionState.value = DataActionState(infinite = true, progress = 0f)
            delay(300L)
            runCatching {
                val backupData = Json.decodeFromString<BackupData>(json)
                folderRepository.insertFolders(backupData.folders)
                val decodedNotes = backupData.notes.map {
                    it.copy(
                        title = Base64.Default.decode(it.title).decodeToString(),
                        content = Base64.Default.decode(it.content).decodeToString()
                    )
                }
                noteRepository.insertNotes(decodedNotes)
                _dataActionState.update { it.copy(progress = 1f) }
            }.onSuccess {
                delay(1500L)
                _dataActionState.value = DataActionState()
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    fun restoreFromOpenNoteJson(json: String) {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch(Dispatchers.IO) {
            _dataActionState.value = DataActionState(infinite = true, progress = 0f)
            delay(300L)
            runCatching {
                val decryptedJson = decryptBackupDataWithCompatibility(json)
                val backupData = Json.decodeFromString<OpenNoteBackupData>(decryptedJson)
                val longToUuidMap: Map<Long?, String> = backupData.folders.associate {
                    it.id to Uuid.random().toString()
                }
                val folders = backupData.folders.map {
                    FolderEntity(
                        id = longToUuidMap[it.id] ?: Uuid.random().toString(),
                        name = it.name,
                        colorValue = it.color?.toLong() ?: defaultFolderColor
                    )
                }
                val notes = backupData.notes.map {
                    val folderId = it.folderId?.let { id -> longToUuidMap[id] }
                    NoteEntity(
                        id = Uuid.random().toString(),
                        title = it.title,
                        content = it.content,
                        createdAt = Instant.fromEpochMilliseconds(it.timestamp).toString(),
                        isDeleted = it.isDeleted,
                        folderId = folderId,
                        noteType = NoteType.MARKDOWN
                    )
                }
                folderRepository.insertFolders(folders)
                noteRepository.insertNotes(notes)
                _dataActionState.update { it.copy(progress = 1f) }
            }.onSuccess {
                delay(1500L)
                _dataActionState.value = DataActionState()
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }
}