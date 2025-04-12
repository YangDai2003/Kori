package org.yangdai.kori.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.entity.NoteEntity

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM notes WHERE is_deleted = 1")
    suspend fun emptyTrash()

    @Query("UPDATE notes SET is_deleted = 0")
    suspend fun restoreAllFromTrash()

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 1")
    fun getTrashNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 0 AND is_template = 0")
    fun getActiveNotesCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM notes WHERE is_template = 1 AND is_deleted = 0")
    fun getTemplatesCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    // 回收站中的笔记 - 按修改时间降序（默认）
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY updated_at DESC")
    fun getNotesInTrashOrderByUpdatedDesc(): Flow<List<NoteEntity>>

    // 回收站中的笔记 - 按修改时间升序
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY updated_at ASC")
    fun getNotesInTrashOrderByUpdatedAsc(): Flow<List<NoteEntity>>

    // 回收站中的笔记 - 按创建时间降序
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY created_at DESC")
    fun getNotesInTrashOrderByCreatedDesc(): Flow<List<NoteEntity>>

    // 回收站中的笔记 - 按创建时间升序
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY created_at ASC")
    fun getNotesInTrashOrderByCreatedAsc(): Flow<List<NoteEntity>>

    // 回收站中的笔记 - 按标题降序
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY title DESC")
    fun getNotesInTrashOrderByNameDesc(): Flow<List<NoteEntity>>

    // 回收站中的笔记 - 按标题升序
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY title ASC")
    fun getNotesInTrashOrderByNameAsc(): Flow<List<NoteEntity>>

    // 获取全部笔记，按名称升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, title ASC")
    fun getAllNotesOrderByNameAsc(): Flow<List<NoteEntity>>

    // 获取全部笔记，按名称降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, title DESC")
    fun getAllNotesOrderByNameDesc(): Flow<List<NoteEntity>>

    // 获取全部笔记，按创建时间升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, created_at ASC")
    fun getAllNotesOrderByCreatedAsc(): Flow<List<NoteEntity>>

    // 获取全部笔记，按创建时间降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, created_at DESC")
    fun getAllNotesOrderByCreatedDesc(): Flow<List<NoteEntity>>

    // 获取全部笔记，按修改时间升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, updated_at ASC")
    fun getAllNotesOrderByUpdatedAsc(): Flow<List<NoteEntity>>

    // 获取全部笔记，按修改时间降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getAllNotesOrderByUpdatedDesc(): Flow<List<NoteEntity>>

    // 根据文件夹 ID 获取笔记，按名称升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, title ASC")
    fun getNotesByFolderIdOrderByNameAsc(folderId: String): Flow<List<NoteEntity>>

    // 根据文件夹 ID 获取笔记，按名称降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, title DESC")
    fun getNotesByFolderIdOrderByNameDesc(folderId: String): Flow<List<NoteEntity>>

    // 根据文件夹 ID 获取笔记，按创建时间升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, created_at ASC")
    fun getNotesByFolderIdOrderByCreatedAsc(folderId: String): Flow<List<NoteEntity>>

    // 根据文件夹 ID 获取笔记，按创建时间降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, created_at DESC")
    fun getNotesByFolderIdOrderByCreatedDesc(folderId: String): Flow<List<NoteEntity>>

    // 根据文件夹 ID 获取笔记，按修改时间升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, updated_at ASC")
    fun getNotesByFolderIdOrderByUpdatedAsc(folderId: String): Flow<List<NoteEntity>>

    // 根据文件夹 ID 获取笔记，按修改时间降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getNotesByFolderIdOrderByUpdatedDesc(folderId: String): Flow<List<NoteEntity>>
    
    // 根据关键词搜索笔记，按名称升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, title ASC")
    fun searchNotesByKeywordOrderByNameAsc(keyword: String): Flow<List<NoteEntity>>

    // 根据关键词搜索笔记，按名称降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, title DESC")
    fun searchNotesByKeywordOrderByNameDesc(keyword: String): Flow<List<NoteEntity>>

    // 根据关键词搜索笔记，按创建时间升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, created_at ASC")
    fun searchNotesByKeywordOrderByCreatedAsc(keyword: String): Flow<List<NoteEntity>>

    // 根据关键词搜索笔记，按创建时间降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, created_at DESC")
    fun searchNotesByKeywordOrderByCreatedDesc(keyword: String): Flow<List<NoteEntity>>

    // 根据关键词搜索笔记，按修改时间升序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, updated_at ASC")
    fun searchNotesByKeywordOrderByUpdatedAsc(keyword: String): Flow<List<NoteEntity>>

    // 根据关键词搜索笔记，按修改时间降序（排除模板笔记），置顶笔记优先
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') AND is_deleted = 0 AND is_template = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun searchNotesByKeywordOrderByUpdatedDesc(keyword: String): Flow<List<NoteEntity>>
    
    // 获取模板笔记相关查询
    
    // 获取所有模板笔记，按名称升序
    @Query("SELECT * FROM notes WHERE is_template = 1 AND is_deleted = 0 ORDER BY title ASC")
    fun getAllTemplatesOrderByNameAsc(): Flow<List<NoteEntity>>
    
    // 获取所有模板笔记，按名称降序
    @Query("SELECT * FROM notes WHERE is_template = 1 AND is_deleted = 0 ORDER BY title DESC")
    fun getAllTemplatesOrderByNameDesc(): Flow<List<NoteEntity>>
    
    // 获取所有模板笔记，按创建时间升序
    @Query("SELECT * FROM notes WHERE is_template = 1 AND is_deleted = 0 ORDER BY created_at ASC")
    fun getAllTemplatesOrderByCreatedAsc(): Flow<List<NoteEntity>>
    
    // 获取所有模板笔记，按创建时间降序
    @Query("SELECT * FROM notes WHERE is_template = 1 AND is_deleted = 0 ORDER BY created_at DESC")
    fun getAllTemplatesOrderByCreatedDesc(): Flow<List<NoteEntity>>
    
    // 获取所有模板笔记，按修改时间升序
    @Query("SELECT * FROM notes WHERE is_template = 1 AND is_deleted = 0 ORDER BY updated_at ASC")
    fun getAllTemplatesOrderByUpdatedAsc(): Flow<List<NoteEntity>>
    
    // 获取所有模板笔记，按修改时间降序
    @Query("SELECT * FROM notes WHERE is_template = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getAllTemplatesOrderByUpdatedDesc(): Flow<List<NoteEntity>>
}

