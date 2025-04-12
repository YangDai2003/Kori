package org.yangdai.kori.data.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yangdai.kori.data.createDataStore
import org.yangdai.kori.data.getDatabaseBuilder
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.getRoomDatabase

actual fun databaseModule(): Module = module {
    single {
        val db = getRoomDatabase(getDatabaseBuilder(androidContext()))
        db
    }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().folderDao() }
    single { createDataStore(androidContext()) }
}