package org.yangdai.kori.data.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.yangdai.kori.data.local.AppDatabase
import org.yangdai.kori.data.repository.DataStoreRepositoryImpl
import org.yangdai.kori.data.repository.FolderRepositoryImpl
import org.yangdai.kori.data.repository.NoteRepositoryImpl
import org.yangdai.kori.data.repository.SnapshotRepositoryImpl
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.FolderRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.domain.repository.SnapshotRepository
import org.yangdai.kori.presentation.screen.file.FileViewModel
import org.yangdai.kori.presentation.screen.folders.FoldersViewModel
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.note.NoteViewModel
import org.yangdai.kori.presentation.screen.template.TemplateViewModel

expect fun platformModule(): Module

fun appModule() = module {
    single<DataStoreRepository> { DataStoreRepositoryImpl(get()) }
    single<FolderRepository> { FolderRepositoryImpl(get<AppDatabase>().folderDao()) }
    single<NoteRepository> { NoteRepositoryImpl(get<AppDatabase>().noteDao()) }
    single<SnapshotRepository> { SnapshotRepositoryImpl(get<AppDatabase>().snapshotDao()) }
    viewModelOf(::MainViewModel)
    viewModelOf(::FoldersViewModel)
    viewModelOf(::NoteViewModel)
    viewModelOf(::TemplateViewModel)
    viewModelOf(::FileViewModel)
}

object KoinInitializer {
    fun init(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
        return startKoin {
            appDeclaration()
            modules(
                listOf(
                    platformModule(),
                    appModule()
                )
            )
        }
    }
}