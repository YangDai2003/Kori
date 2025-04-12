package org.yangdai.kori.presentation.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.SortDirection

class FoldersViewModel(
    private val folderRepository: FolderRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _foldersWithNoteCounts =
        MutableStateFlow<List<FolderDao.FolderWithNoteCount>>(emptyList())
    val foldersWithNoteCounts: StateFlow<List<FolderDao.FolderWithNoteCount>> = _foldersWithNoteCounts

    var folderSortType by mutableStateOf(FolderSortType.NAME)
        private set

    var folderSortDirection by mutableStateOf(SortDirection.ASC)
        private set

    init {
        loadFoldersWithNoteCounts()
    }

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

    fun setFolderSorting(sortType: FolderSortType, direction: SortDirection) {
        folderSortType = sortType
        folderSortDirection = direction
        loadFoldersWithNoteCounts() // 更新文件夹及笔记数量
    }
}