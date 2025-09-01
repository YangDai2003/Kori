package org.yangdai.kori.data.di

import knet.ConnectivityObserver
import knet.JvmConnectivityObserver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yangdai.kori.data.createDataStore
import org.yangdai.kori.data.getDatabaseBuilder
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.getRoomDatabase

actual fun platformModule(): Module = module {
    single {
        val db = getRoomDatabase(getDatabaseBuilder())
        db
    }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().folderDao() }
    single { createDataStore() }
    single<ConnectivityObserver> { JvmConnectivityObserver() }
}