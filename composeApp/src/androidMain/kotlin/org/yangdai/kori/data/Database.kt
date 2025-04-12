package org.yangdai.kori.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.dbFileName

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath(dbFileName)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}