package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.domain.sort.NoteSortType

interface NoteRepository {
    suspend fun insertNote(note: NoteEntity): Long
    suspend fun insertNotes(notes: List<NoteEntity>)
    suspend fun updateNote(note: NoteEntity)
    suspend fun deleteNote(note: NoteEntity)
    suspend fun deleteNoteById(id: String)
    suspend fun deleteNoteByIds(ids: List<String>)
    suspend fun emptyTrash()
    suspend fun restoreAllFromTrash()
    fun getTrashNotesCount(): Flow<Int>
    fun getActiveNotesCount(): Flow<Int>
    fun getTemplatesCount(): Flow<Int>
    suspend fun getNoteById(id: String): NoteEntity?

    /**
     * 获取回收站中的笔记
     * @param sortType 排序字段
     */
    fun getNotesInTrash(
        sortType: NoteSortType = NoteSortType.UPDATE_TIME_DESC
    ): Flow<List<NoteEntity>>

    /**
     * 获取所有活跃笔记（排除模板笔记）
     * @param sortType 排序字段
     */
    fun getAllNotes(
        sortType: NoteSortType = NoteSortType.UPDATE_TIME_DESC
    ): Flow<List<NoteEntity>>

    /**
     * 按文件夹查询笔记（排除模板笔记）
     * @param folderId 文件夹ID
     * @param sortType 排序字段
     */
    fun getNotesByFolderId(
        folderId: String,
        sortType: NoteSortType = NoteSortType.UPDATE_TIME_DESC
    ): Flow<List<NoteEntity>>

    /**
     * 搜索笔记（排除模板笔记）
     * @param keyword 搜索关键词
     * @param sortType 排序字段
     */
    fun searchNotesByKeyword(
        keyword: String,
        sortType: NoteSortType = NoteSortType.UPDATE_TIME_DESC
    ): Flow<List<NoteEntity>>

    /**
     * 获取所有模板笔记
     * @param sortType 排序字段
     */
    fun getAllTemplates(
        sortType: NoteSortType = NoteSortType.UPDATE_TIME_DESC
    ): Flow<List<NoteEntity>>
}
