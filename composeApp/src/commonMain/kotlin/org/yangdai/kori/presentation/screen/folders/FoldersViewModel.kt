package org.yangdai.kori.presentation.screen.folders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.presentation.util.Constants

class FoldersViewModel(
    private val folderRepository: FolderRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    var folderSortType by mutableStateOf(FolderSortType.CREATE_TIME_DESC)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val foldersMap: StateFlow<Map<Boolean, List<FolderDao.FolderWithNoteCount>>> =
        dataStoreRepository
            .intFlow(Constants.Preferences.FOLDER_SORT_TYPE)
            .map {
                FolderSortType.fromValue(it)
                    .also { sortType -> folderSortType = sortType }
            }
            .distinctUntilChanged()
            .flatMapLatest { sortType ->
                folderRepository.getFoldersWithNoteCounts(sortType)
                    .map { folders -> folders.groupBy { it.folder.isStarred } }
                    .flowOn(Dispatchers.Default)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyMap()
            )

    fun createFolder(folderEntity: FolderEntity) {
        viewModelScope.launch {
            folderRepository.insertFolder(folderEntity)
        }
    }

    fun updateFolder(folderEntity: FolderEntity) {
        viewModelScope.launch {
            folderRepository.updateFolder(folderEntity)
        }
    }

    fun deleteFolder(folderEntity: FolderEntity) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folderEntity)
        }
    }

    fun setFolderSorting(sortType: FolderSortType) {
        viewModelScope.launch {
            dataStoreRepository.putInt(Constants.Preferences.FOLDER_SORT_TYPE, sortType.value)
        }
    }
}