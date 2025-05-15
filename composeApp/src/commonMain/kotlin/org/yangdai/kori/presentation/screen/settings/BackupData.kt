package org.yangdai.kori.presentation.screen.settings

import kotlinx.serialization.Serializable
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.NoteEntity

@Serializable
data class BackupData(
    val notes: List<NoteEntity>,
    val folders: List<FolderEntity>
)
