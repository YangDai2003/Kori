package org.yangdai.kori.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.dao.NoteDao
import org.yangdai.kori.data.local.dao.SnapshotDao
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.SnapshotEntity

internal const val dbFileName = "kori.db"

@Database(
    entities = [NoteEntity::class, FolderEntity::class, SnapshotEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun snapshotDao(): SnapshotDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}