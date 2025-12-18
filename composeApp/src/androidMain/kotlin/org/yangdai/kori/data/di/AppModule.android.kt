package org.yangdai.kori.data.di

import knet.AndroidConnectivityObserver
import knet.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yangdai.kori.data.createDataStore
import org.yangdai.kori.data.getDatabaseBuilder
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.local.getRoomDatabase

actual fun platformModule(): Module = module {
    single { getRoomDatabase(getDatabaseBuilder(androidContext())) }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().folderDao() }
    single { createDataStore(androidContext()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
}