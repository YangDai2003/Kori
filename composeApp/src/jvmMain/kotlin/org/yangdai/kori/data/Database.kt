package org.yangdai.kori.data

import androidx.room.Room
import androidx.room.RoomDatabase
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.dbFileName
import org.yangdai.kori.koriDirPath
import java.nio.file.Files

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    if (!Files.exists(koriDirPath)) Files.createDirectories(koriDirPath)
    val dbFilePath = koriDirPath.resolve(dbFileName)
    return Room.databaseBuilder<AppDatabase>(dbFilePath.toAbsolutePath().toString())
}