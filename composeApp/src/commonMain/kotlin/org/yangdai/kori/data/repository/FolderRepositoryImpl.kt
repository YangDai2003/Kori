package org.yangdai.kori.data.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.SortDirection

class FolderRepositoryImpl(
    private val dao: FolderDao
) : FolderRepository {
    override suspend fun insertFolder(folder: FolderEntity): Long {
        return dao.insertFolder(folder)
    }

    override suspend fun insertFolders(folders: List<FolderEntity>) {
        dao.insertFolders(folders)
    }

    override suspend fun updateFolder(folder: FolderEntity) {
        dao.updateFolder(folder)
    }

    override suspend fun deleteFolder(folder: FolderEntity) {
        dao.deleteFolder(folder)
    }

    override suspend fun deleteFolderById(id: String) {
        dao.deleteFolderById(id)
    }

    override fun getFoldersWithNoteCounts(
        sortField: FolderSortType,
        sortDirection: SortDirection
    ): Flow<List<FolderWithNoteCount>> {
        return when (sortField) {
            FolderSortType.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getFoldersWithNoteCountsByNameAsc()
                    SortDirection.DESC -> dao.getFoldersWithNoteCountsByNameDesc()
                }
            }
            FolderSortType.CREATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getFoldersWithNoteCountsByCreatedAsc()
                    SortDirection.DESC -> dao.getFoldersWithNoteCountsByCreatedDesc()
                }
            }
        }
    }
}
