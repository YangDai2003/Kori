package org.yangdai.kori.presentation.screen.template

import androidx.compose.runtime.Immutable
import org.yangdai.kori.data.local.entity.NoteType

@Immutable
data class TemplateEditingState(
    val id: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val isDeleted: Boolean = false,
    val noteType: NoteType = NoteType.PLAIN_TEXT
)