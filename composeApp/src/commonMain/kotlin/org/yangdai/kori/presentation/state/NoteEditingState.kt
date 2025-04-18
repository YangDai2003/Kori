package org.yangdai.kori.presentation.state

import org.yangdai.kori.data.local.entity.NoteType

data class NoteEditingState(
    val id: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val folderId: String? = null,
    val noteType: NoteType = NoteType.UNSPECIFIED,
    val isTemplate: Boolean = false
)