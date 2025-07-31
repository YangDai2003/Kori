package org.yangdai.kori.data.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.yangdai.kori.data.repository.DataStoreRepositoryImpl
import org.yangdai.kori.data.repository.FolderRepositoryImpl
import org.yangdai.kori.data.repository.NoteRepositoryImpl
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.screen.file.FileViewModel
import org.yangdai.kori.presentation.screen.folders.FoldersViewModel
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.note.NoteViewModel
import org.yangdai.kori.presentation.screen.settings.DataViewModel
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.screen.template.TemplateViewModel

expect fun databaseModule(): Module

fun appModule() = module {
    single<FolderRepository> { FolderRepositoryImpl(get()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<DataStoreRepository> { DataStoreRepositoryImpl(get()) }
    viewModelOf(::MainViewModel)
    viewModelOf(::FoldersViewModel)
    viewModelOf(::NoteViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::TemplateViewModel)
    viewModelOf(::FileViewModel)
    viewModelOf(::DataViewModel)
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