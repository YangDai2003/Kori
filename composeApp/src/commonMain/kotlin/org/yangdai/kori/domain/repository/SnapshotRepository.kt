package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.entity.SnapshotEntity

interface SnapshotRepository {
    suspend fun insertSnapshot(snapshot: SnapshotEntity): Long
    suspend fun deleteSnapshot(snapshot: SnapshotEntity)
    suspend fun deleteSnapshotById(id: Long)
    suspend fun deleteSnapshotsByNoteId(noteId: String)
    suspend fun cleanupOldSnapshots(noteId: String, limit: Int = 5)
    suspend fun getSnapshotById(id: String): SnapshotEntity?
    fun getSnapshotsByNoteIdFlow(noteId: String): Flow<List<SnapshotEntity>>
    suspend fun saveSnapshotForNote(noteId: String, title: String, content: String)
}