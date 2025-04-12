package org.yangdai.kori.data.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.dao.NoteDao
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.sort.NoteSortType
import org.yangdai.kori.domain.sort.SortDirection

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {
    override suspend fun insertNote(note: NoteEntity): Long {
        return dao.insertNote(note)
    }

    override suspend fun insertNotes(notes: List<NoteEntity>) {
        dao.insertNotes(notes)
    }

    override suspend fun updateNote(note: NoteEntity) {
        dao.updateNote(note)
    }

    override suspend fun deleteNote(note: NoteEntity) {
        dao.deleteNote(note)
    }

    override suspend fun deleteNoteById(id: String) {
        dao.deleteNoteById(id)
    }

    override suspend fun emptyTrash() {
        dao.emptyTrash()
    }

    override suspend fun restoreAllFromTrash() {
        dao.restoreAllFromTrash()
    }

    override fun getTrashNotesCount(): Flow<Int> {
        return dao.getTrashNotesCount()
    }

    override fun getActiveNotesCount(): Flow<Int> {
        return dao.getActiveNotesCount()
    }
    
    override fun getTemplatesCount(): Flow<Int> {
        return dao.getTemplatesCount()
    }

    override suspend fun getNoteById(id: String): NoteEntity? {
        return dao.getNoteById(id)
    }

    override fun getNotesInTrash(
        sortField: NoteSortType,
        sortDirection: SortDirection
    ): Flow<List<NoteEntity>> {
        return when (sortField) {
            NoteSortType.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getNotesInTrashOrderByNameAsc()
                    SortDirection.DESC -> dao.getNotesInTrashOrderByNameDesc()
                }
            }
            NoteSortType.CREATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getNotesInTrashOrderByCreatedAsc()
                    SortDirection.DESC -> dao.getNotesInTrashOrderByCreatedDesc()
                }
            }
            NoteSortType.UPDATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getNotesInTrashOrderByUpdatedAsc()
                    SortDirection.DESC -> dao.getNotesInTrashOrderByUpdatedDesc()
                }
            }
        }
    }

    override fun getAllNotes(
        sortField: NoteSortType,
        sortDirection: SortDirection
    ): Flow<List<NoteEntity>> {
        return when (sortField) {
            NoteSortType.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getAllNotesOrderByNameAsc()
                    SortDirection.DESC -> dao.getAllNotesOrderByNameDesc()
                }
            }
            NoteSortType.CREATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getAllNotesOrderByCreatedAsc()
                    SortDirection.DESC -> dao.getAllNotesOrderByCreatedDesc()
                }
            }
            NoteSortType.UPDATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getAllNotesOrderByUpdatedAsc()
                    SortDirection.DESC -> dao.getAllNotesOrderByUpdatedDesc()
                }
            }
        }
    }

    override fun getNotesByFolderId(
        folderId: String,
        sortField: NoteSortType,
        sortDirection: SortDirection
    ): Flow<List<NoteEntity>> {
        return when (sortField) {
            NoteSortType.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getNotesByFolderIdOrderByNameAsc(folderId)
                    SortDirection.DESC -> dao.getNotesByFolderIdOrderByNameDesc(folderId)
                }
            }
            NoteSortType.CREATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getNotesByFolderIdOrderByCreatedAsc(folderId)
                    SortDirection.DESC -> dao.getNotesByFolderIdOrderByCreatedDesc(folderId)
                }
            }
            NoteSortType.UPDATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getNotesByFolderIdOrderByUpdatedAsc(folderId)
                    SortDirection.DESC -> dao.getNotesByFolderIdOrderByUpdatedDesc(folderId)
                }
            }
        }
    }

    override fun searchNotesByKeyword(
        keyword: String,
        sortField: NoteSortType,
        sortDirection: SortDirection
    ): Flow<List<NoteEntity>> {
        return when (sortField) {
            NoteSortType.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.searchNotesByKeywordOrderByNameAsc(keyword)
                    SortDirection.DESC -> dao.searchNotesByKeywordOrderByNameDesc(keyword)
                }
            }
            NoteSortType.CREATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.searchNotesByKeywordOrderByCreatedAsc(keyword)
                    SortDirection.DESC -> dao.searchNotesByKeywordOrderByCreatedDesc(keyword)
                }
            }
            NoteSortType.UPDATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.searchNotesByKeywordOrderByUpdatedAsc(keyword)
                    SortDirection.DESC -> dao.searchNotesByKeywordOrderByUpdatedDesc(keyword)
                }
            }
        }
    }
    
    override fun getAllTemplates(
        sortField: NoteSortType,
        sortDirection: SortDirection
    ): Flow<List<NoteEntity>> {
        return when (sortField) {
            NoteSortType.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getAllTemplatesOrderByNameAsc()
                    SortDirection.DESC -> dao.getAllTemplatesOrderByNameDesc()
                }
            }
            NoteSortType.CREATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getAllTemplatesOrderByCreatedAsc()
                    SortDirection.DESC -> dao.getAllTemplatesOrderByCreatedDesc()
                }
            }
            NoteSortType.UPDATED -> {
                when (sortDirection) {
                    SortDirection.ASC -> dao.getAllTemplatesOrderByUpdatedAsc()
                    SortDirection.DESC -> dao.getAllTemplatesOrderByUpdatedDesc()
                }
            }
        }
    }
}
