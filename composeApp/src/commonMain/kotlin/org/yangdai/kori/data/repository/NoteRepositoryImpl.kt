package org.yangdai.kori.data.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.dao.NoteDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.NoteSortType

class NoteRepositoryImpl(private val dao: NoteDao) : NoteRepository {
    override suspend fun insertNote(note: NoteEntity): Long {
        return dao.insertNote(note)
    }

    override suspend fun insertNotes(notes: List<NoteEntity>) = dao.insertNotes(notes)

    override suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)

    override suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)

    override suspend fun deleteNoteById(id: String) = dao.deleteNoteById(id)

    override suspend fun deleteNoteByIds(ids: List<String>) = dao.deleteNotesByIds(ids)

    override suspend fun deleteAllNotes() = dao.deleteAllNotes()

    override suspend fun emptyTrash() = dao.emptyTrash()

    override suspend fun restoreAllFromTrash() = dao.restoreAllFromTrash()


    override fun getTrashNotesCount(): Flow<Int> = dao.getTrashNotesCount()
    override fun getActiveNotesCount(): Flow<Int> = dao.getActiveNotesCount()
    override fun getTemplatesCount(): Flow<Int> = dao.getTemplatesCount()


    override suspend fun getNoteById(id: String): NoteEntity? {
        return dao.getNoteById(id)
    }

    override fun getNotesInTrash(sortType: NoteSortType): Flow<List<NoteEntity>> = when (sortType) {
        NoteSortType.NAME_ASC -> dao.getNotesInTrashOrderByNameAsc()
        NoteSortType.NAME_DESC -> dao.getNotesInTrashOrderByNameDesc()
        NoteSortType.CREATE_TIME_ASC -> dao.getNotesInTrashOrderByCreatedAsc()
        NoteSortType.CREATE_TIME_DESC -> dao.getNotesInTrashOrderByCreatedDesc()
        NoteSortType.UPDATE_TIME_ASC -> dao.getNotesInTrashOrderByUpdatedAsc()
        NoteSortType.UPDATE_TIME_DESC -> dao.getNotesInTrashOrderByUpdatedDesc()
    }

    override fun getAllNotes(sortType: NoteSortType): Flow<List<NoteEntity>> = when (sortType) {
        NoteSortType.NAME_ASC -> dao.getAllNotesOrderByNameAsc()
        NoteSortType.NAME_DESC -> dao.getAllNotesOrderByNameDesc()
        NoteSortType.CREATE_TIME_ASC -> dao.getAllNotesOrderByCreatedAsc()
        NoteSortType.CREATE_TIME_DESC -> dao.getAllNotesOrderByCreatedDesc()
        NoteSortType.UPDATE_TIME_ASC -> dao.getAllNotesOrderByUpdatedAsc()
        NoteSortType.UPDATE_TIME_DESC -> dao.getAllNotesOrderByUpdatedDesc()
    }

    override fun getNotesByFolderId(
        folderId: String, sortType: NoteSortType
    ): Flow<List<NoteEntity>> = when (sortType) {
        NoteSortType.NAME_ASC -> dao.getNotesByFolderIdOrderByNameAsc(folderId)
        NoteSortType.NAME_DESC -> dao.getNotesByFolderIdOrderByNameDesc(folderId)
        NoteSortType.CREATE_TIME_ASC -> dao.getNotesByFolderIdOrderByCreatedAsc(folderId)
        NoteSortType.CREATE_TIME_DESC -> dao.getNotesByFolderIdOrderByCreatedDesc(folderId)
        NoteSortType.UPDATE_TIME_ASC -> dao.getNotesByFolderIdOrderByUpdatedAsc(folderId)
        NoteSortType.UPDATE_TIME_DESC -> dao.getNotesByFolderIdOrderByUpdatedDesc(folderId)
    }

    override fun searchNotesByKeyword(
        keyword: String, sortType: NoteSortType
    ): Flow<List<NoteEntity>> = when (sortType) {
        NoteSortType.NAME_ASC -> dao.searchNotesByKeywordOrderByNameAsc(keyword)
        NoteSortType.NAME_DESC -> dao.searchNotesByKeywordOrderByNameDesc(keyword)
        NoteSortType.CREATE_TIME_ASC -> dao.searchNotesByKeywordOrderByCreatedAsc(keyword)
        NoteSortType.CREATE_TIME_DESC -> dao.searchNotesByKeywordOrderByCreatedDesc(keyword)
        NoteSortType.UPDATE_TIME_ASC -> dao.searchNotesByKeywordOrderByUpdatedAsc(keyword)
        NoteSortType.UPDATE_TIME_DESC -> dao.searchNotesByKeywordOrderByUpdatedDesc(keyword)
    }

    override fun getAllTemplates(sortType: NoteSortType): Flow<List<NoteEntity>> = when (sortType) {
        NoteSortType.NAME_ASC -> dao.getAllTemplatesOrderByNameAsc()
        NoteSortType.NAME_DESC -> dao.getAllTemplatesOrderByNameDesc()
        NoteSortType.CREATE_TIME_ASC -> dao.getAllTemplatesOrderByCreatedAsc()
        NoteSortType.CREATE_TIME_DESC -> dao.getAllTemplatesOrderByCreatedDesc()
        NoteSortType.UPDATE_TIME_ASC -> dao.getAllTemplatesOrderByUpdatedAsc()
        NoteSortType.UPDATE_TIME_DESC -> dao.getAllTemplatesOrderByUpdatedDesc()
    }
}
