package org.yangdai.kori.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yangdai.kori.data.local.dao.SnapshotDao
import org.yangdai.kori.data.local.entity.SnapshotEntity
import org.yangdai.kori.domain.repository.SnapshotRepository
import kotlin.io.encoding.Base64

class SnapshotRepositoryImpl(private val dao: SnapshotDao) : SnapshotRepository {

    override suspend fun deleteSnapshot(snapshot: SnapshotEntity) =
        dao.deleteSnapshotById(snapshot.id)

    override suspend fun deleteSnapshotsByNoteId(noteId: String) =
        dao.deleteSnapshotsByNoteId(noteId)

    override suspend fun deleteAllSnapshots() =
        dao.deleteAllSnapshots()

    override fun getSnapshotsByNoteIdFlow(noteId: String): Flow<List<SnapshotEntity>> {
        return dao.getSnapshotsByNoteIdFlow(noteId).map { snapshots ->
            snapshots.map { snapshot ->
                snapshot.copy(content = Base64.decode(snapshot.content).decodeToString())
            }
        }
    }

    override suspend fun saveNewSnapshotForNote(noteId: String, content: String) {
        val lastSnapshot = dao.getLastSnapshotByNoteId(noteId)?.let {
            Base64.decode(it.content).decodeToString()
        }
        if (lastSnapshot == content) return
        // Create a new snapshot with the provided data
        val snapshot =
            SnapshotEntity(noteId = noteId, content = Base64.encode(content.encodeToByteArray()))
        // Insert the new snapshot
        dao.insertSnapshot(snapshot)
        // Clean up old snapshots to maintain the limit (default 5)
        dao.deleteExcessSnapshots(noteId, 5)
    }
}