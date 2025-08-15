package org.yangdai.kori.data

import androidx.room.Room
import androidx.room.RoomDatabase
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.dbFileName
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val userHome: String = System.getProperty("user.home")
    val koriDir = File(userHome, ".kori")
    if (!koriDir.exists()) koriDir.mkdirs()
    val dbFile = File(koriDir, dbFileName)
    return Room.databaseBuilder<AppDatabase>(dbFile.absolutePath)
}