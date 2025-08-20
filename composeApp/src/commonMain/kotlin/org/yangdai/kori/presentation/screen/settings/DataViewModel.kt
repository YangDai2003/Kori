package org.yangdai.kori.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kfile.PlatformFile
import kfile.getExtension
import kfile.getFileName
import kfile.getLastModified
import kfile.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DataViewModel(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val foldersWithNoteCounts: StateFlow<List<FolderDao.FolderWithNoteCount>> =
        folderRepository.getFoldersWithNoteCounts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Companion.WhileSubscribed(5_000L),
                initialValue = emptyList()
            )

    private val _dataActionState = MutableStateFlow(DataActionState())
    val dataActionState = _dataActionState.asStateFlow()
    private var dataActionJob: Job? = null

    fun cancelDataAction() {
        dataActionJob?.cancel()
        _dataActionState.value = DataActionState()
    }

    fun resetDatabase() {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch {
            _dataActionState.value = DataActionState(infinite = true, progress = 0f)
            delay(300L)
            runCatching {
                noteRepository.deleteAllNotes()
                folderRepository.deleteAllFolders()
            }.onSuccess {
                _dataActionState.update { it.copy(progress = 1f) }
                delay(3000L)
                _dataActionState.update { it.copy(progress = -1f) }
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
                    ) NoteType.MARKDOWN else NoteType.PLAIN_TEXT
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
            }.onSuccess {
                _dataActionState.update { it.copy(progress = 1f) }
                delay(3000L)
                _dataActionState.update { it.copy(progress = -1f) }
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
                val notes = noteRepository.getAllNotes().firstOrNull() ?: emptyList()
                val folders = folderRepository.getFoldersWithNoteCounts()
                    .firstOrNull()
                    ?.map { it.folder }
                    ?: emptyList()
                val backupData = BackupData(notes, folders)
                val jsonString = Json.encodeToString(backupData)
                onCreated(jsonString)
            }.onSuccess {
                _dataActionState.update { it.copy(progress = 1f) }
                delay(1000L)
                _dataActionState.update { it.copy(progress = -1f) }
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }

    fun restoreFromJson(json: String) {
        dataActionJob?.cancel()
        dataActionJob = viewModelScope.launch(Dispatchers.IO) {
            _dataActionState.value = DataActionState(progress = 0f)
            delay(300L)
            runCatching {
                val backupData = Json.decodeFromString<BackupData>(json)
                _dataActionState.update { it.copy(progress = 0.33f) }
                folderRepository.insertFolders(backupData.folders)
                _dataActionState.update { it.copy(progress = 0.67f) }
                noteRepository.insertNotes(backupData.notes)
            }.onSuccess {
                _dataActionState.update { it.copy(progress = 1f) }
                delay(3000L)
                _dataActionState.update { it.copy(progress = -1f) }
            }.onFailure { throwable ->
                _dataActionState.update { it.copy(message = throwable.message ?: "Error :(") }
            }
        }
    }
}