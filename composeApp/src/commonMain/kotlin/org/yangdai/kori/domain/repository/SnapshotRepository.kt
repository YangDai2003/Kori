package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.entity.SnapshotEntity

interface SnapshotRepository {
    suspend fun deleteSnapshot(snapshot: SnapshotEntity)
    suspend fun deleteSnapshotsByNoteId(noteId: String)
    suspend fun deleteAllSnapshots()
    fun getSnapshotsByNoteIdFlow(noteId: String): Flow<List<SnapshotEntity>>
    suspend fun saveNewSnapshotForNote(noteId: String, content: String)
}