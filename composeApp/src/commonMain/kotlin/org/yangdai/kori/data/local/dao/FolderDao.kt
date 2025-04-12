package org.yangdai.kori.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.entity.FolderEntity

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)

    /**
     * 获取所有文件夹及其包含的笔记数量（按名称升序，星标文件夹优先）
     *
     * @return 包含文件夹及其笔记数量的Flow
     */
    @Transaction
    @Query("""
        SELECT f.*,
               (SELECT COUNT(*) FROM notes WHERE folder_id = f.id AND is_deleted = 0) AS noteCount
        FROM folders f
        ORDER BY f.is_starred DESC, f.name ASC
    """)
    fun getFoldersWithNoteCountsByNameAsc(): Flow<List<FolderWithNoteCount>>

    /**
     * 获取所有文件夹及其包含的笔记数量（按名称降序，星标文件夹优先）
     *
     * @return 包含文件夹及其笔记数量的Flow
     */
    @Transaction
    @Query("""
        SELECT f.*,
               (SELECT COUNT(*) FROM notes WHERE folder_id = f.id AND is_deleted = 0) AS noteCount
        FROM folders f
        ORDER BY f.is_starred DESC, f.name DESC
    """)
    fun getFoldersWithNoteCountsByNameDesc(): Flow<List<FolderWithNoteCount>>

    /**
     * 获取所有文件夹及其包含的笔记数量（按创建时间升序，星标文件夹优先）
     *
     * @return 包含文件夹及其笔记数量的Flow
     */
    @Transaction
    @Query("""
        SELECT f.*,
               (SELECT COUNT(*) FROM notes WHERE folder_id = f.id AND is_deleted = 0) AS noteCount
        FROM folders f
        ORDER BY f.is_starred DESC, f.created_at ASC
    """)
    fun getFoldersWithNoteCountsByCreatedAsc(): Flow<List<FolderWithNoteCount>>

    /**
     * 获取所有文件夹及其包含的笔记数量（按创建时间降序，星标文件夹优先）
     *
     * @return 包含文件夹及其笔记数量的Flow
     */
    @Transaction
    @Query("""
        SELECT f.*,
               (SELECT COUNT(*) FROM notes WHERE folder_id = f.id AND is_deleted = 0) AS noteCount
        FROM folders f
        ORDER BY f.is_starred DESC, f.created_at DESC
    """)
    fun getFoldersWithNoteCountsByCreatedDesc(): Flow<List<FolderWithNoteCount>>

    /**
     * 表示文件夹及其包含的笔记数量的数据类
     */
    data class FolderWithNoteCount(
        @Embedded val folder: FolderEntity,
        val noteCount: Int
    )
}
