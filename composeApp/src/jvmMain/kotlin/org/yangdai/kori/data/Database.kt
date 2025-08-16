package org.yangdai.kori.data

import androidx.room.Room
import androidx.room.RoomDatabase
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.dbFileName
import java.nio.file.Files
import java.nio.file.Paths

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val userHome: String = System.getProperty("user.home")
    val koriDirPath = Paths.get(userHome, ".kori")
    if (!Files.exists(koriDirPath)) Files.createDirectories(koriDirPath)
    val dbFilePath = koriDirPath.resolve(dbFileName)
    return Room.databaseBuilder<AppDatabase>(dbFilePath.toAbsolutePath().toString())
}