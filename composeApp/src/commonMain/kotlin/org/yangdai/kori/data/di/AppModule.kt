package org.yangdai.kori.data.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.yangdai.kori.AppViewModel
import org.yangdai.kori.data.repository.DataStoreRepositoryImpl
import org.yangdai.kori.data.repository.FolderRepositoryImpl
import org.yangdai.kori.data.repository.NoteRepositoryImpl
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository

expect fun databaseModule(): Module

fun appModule() = module {
    single<FolderRepository> { FolderRepositoryImpl(get()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<DataStoreRepository> { DataStoreRepositoryImpl(get()) }
    viewModelOf(::AppViewModel)
}

object KoinInitializer {
    fun init(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
        return startKoin {
            appDeclaration()
            modules(
                listOf(
                    databaseModule(),
                    appModule()
                )
            )
        }
    }
}