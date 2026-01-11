package org.yangdai.kori.data.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.dao.SnapshotDao
import org.yangdai.kori.data.local.entity.SnapshotEntity
import org.yangdai.kori.domain.repository.SnapshotRepository

class SnapshotRepositoryImpl(private val dao: SnapshotDao) : SnapshotRepository {

    override suspend fun deleteSnapshot(snapshot: SnapshotEntity) =
        dao.deleteSnapshotById(snapshot.id)

    override suspend fun deleteSnapshotById(id: Long) =
        dao.deleteSnapshotById(id)

    override suspend fun deleteSnapshotsByNoteId(noteId: String) =
        dao.deleteSnapshotsByNoteId(noteId)

    override suspend fun deleteAllSnapshots() =
        dao.deleteAllSnapshots()

    override suspend fun getSnapshotById(id: String): SnapshotEntity? {
        return dao.getSnapshotById(id)
    }

    override fun getSnapshotsByNoteIdFlow(noteId: String): Flow<List<SnapshotEntity>> {
        return dao.getSnapshotsByNoteIdFlow(noteId)
    }

    override suspend fun saveSnapshotForNote(noteId: String, title: String, content: String) {
        // Create a new snapshot with the provided data
        val snapshot = SnapshotEntity(noteId = noteId, title = title, content = content)
        // Insert the new snapshot
        dao.insertSnapshot(snapshot)
        // Clean up old snapshots to maintain the limit (default 5)
        dao.deleteExcessSnapshots(noteId, 5)
    }
}