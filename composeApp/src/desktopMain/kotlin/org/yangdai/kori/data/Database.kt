package org.yangdai.kori.data

import androidx.room.Room
import androidx.room.RoomDatabase
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.dbFileName
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), dbFileName)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}