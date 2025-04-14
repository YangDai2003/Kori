package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.domain.sort.FolderSortType

interface FolderRepository {
    suspend fun insertFolder(folder: FolderEntity): Long
    suspend fun insertFolders(folders: List<FolderEntity>)
    suspend fun updateFolder(folder: FolderEntity)
    suspend fun deleteFolder(folder: FolderEntity)
    suspend fun deleteFolderById(id: String)

    /**
     * 获取所有文件夹及其笔记数量
     * @param sortType 排序字段
     */
    fun getFoldersWithNoteCounts(
        sortType: FolderSortType = FolderSortType.CREATE_TIME_DESC
    ): Flow<List<FolderWithNoteCount>>
}
