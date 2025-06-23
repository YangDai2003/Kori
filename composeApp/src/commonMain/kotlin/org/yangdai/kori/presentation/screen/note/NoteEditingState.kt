package org.yangdai.kori.presentation.screen.note

import androidx.compose.runtime.Immutable
import org.yangdai.kori.data.local.entity.NoteType

@Immutable
data class NoteEditingState(
    val id: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val folderId: String? = null,
    val noteType: NoteType = NoteType.PLAIN_TEXT,
    val isTemplate: Boolean = false
)