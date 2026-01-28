package org.yangdai.kori.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.yangdai.kori.data.local.entity.SnapshotEntity

@Dao
interface SnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SnapshotEntity): Long

    @Query("SELECT * FROM snapshots WHERE note_id = :noteId ORDER BY created_at DESC")
    fun getSnapshotsByNoteIdFlow(noteId: String): Flow<List<SnapshotEntity>>

    @Query("SELECT * FROM snapshots WHERE note_id = :noteId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLastSnapshotByNoteId(noteId: String): SnapshotEntity?

    @Query("DELETE FROM snapshots WHERE id = :id")
    suspend fun deleteSnapshotById(id: Long)

    @Query("DELETE FROM snapshots WHERE note_id = :noteId")
    suspend fun deleteSnapshotsByNoteId(noteId: String)

    @Query(
        """
        DELETE FROM snapshots 
        WHERE note_id = :noteId 
        AND id NOT IN (
            SELECT id FROM (
                SELECT id FROM snapshots 
                WHERE note_id = :noteId 
                ORDER BY created_at DESC 
                LIMIT :maxCount
            )
        )
    """
    )
    suspend fun deleteExcessSnapshots(noteId: String, maxCount: Int)

    @Query("DELETE FROM snapshots")
    suspend fun deleteAllSnapshots()
}