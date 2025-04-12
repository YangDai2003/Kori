package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.SortDirection

interface FolderRepository {
    suspend fun insertFolder(folder: FolderEntity): Long
    suspend fun insertFolders(folders: List<FolderEntity>)
    suspend fun updateFolder(folder: FolderEntity)
    suspend fun deleteFolder(folder: FolderEntity)
    suspend fun deleteFolderById(id: String)
    
    /**
     * 获取所有文件夹及其笔记数量
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     */
    fun getFoldersWithNoteCounts(
        sortField: FolderSortType = FolderSortType.NAME,
        sortDirection: SortDirection = SortDirection.ASC
    ): Flow<List<FolderWithNoteCount>>
}
