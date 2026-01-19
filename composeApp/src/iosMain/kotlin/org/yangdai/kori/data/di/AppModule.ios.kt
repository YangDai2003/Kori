package org.yangdai.kori.data.di

import knet.ConnectivityObserver
import knet.IOSConnectivityObserver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yangdai.kori.data.createDataStore
import org.yangdai.kori.data.getDatabaseBuilder
import org.yangdai.kori.data.local.getRoomDatabase

actual fun platformModule(): Module = module {
    single { getRoomDatabase(getDatabaseBuilder()) }
    single { createDataStore() }
    single<ConnectivityObserver> { IOSConnectivityObserver() }
}