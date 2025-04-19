package org.yangdai.kori.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.sort.FolderSortType

class FolderRepositoryImpl(
    private val dao: FolderDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FolderRepository {
    override suspend fun insertFolder(folder: FolderEntity): Long = withContext(ioDispatcher) {
        dao.insertFolder(folder)
    }

    override suspend fun insertFolders(folders: List<FolderEntity>) = withContext(ioDispatcher) {
        dao.insertFolders(folders)
    }

    override suspend fun updateFolder(folder: FolderEntity) = withContext(ioDispatcher) {
        dao.updateFolder(folder)
    }

    override suspend fun deleteFolder(folder: FolderEntity) = withContext(ioDispatcher) {
        dao.deleteFolder(folder)
    }

    override suspend fun deleteFolderById(id: String) = withContext(ioDispatcher) {
        dao.deleteFolderById(id)
    }

    override fun getFoldersWithNoteCounts(sortType: FolderSortType): Flow<List<FolderWithNoteCount>> =
        when (sortType) {
            FolderSortType.NAME_ASC -> dao.getFoldersWithNoteCountsByNameAsc()
            FolderSortType.NAME_DESC -> dao.getFoldersWithNoteCountsByNameDesc()
            FolderSortType.CREATE_TIME_ASC -> dao.getFoldersWithNoteCountsByCreatedAsc()
            FolderSortType.CREATE_TIME_DESC -> dao.getFoldersWithNoteCountsByCreatedDesc()
        }.flowOn(ioDispatcher)
}
