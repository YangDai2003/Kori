package org.yangdai.kori.presentation.screen.file

import androidx.compose.runtime.Immutable
import org.yangdai.kori.data.local.entity.NoteType

@Immutable
data class FileEditingState(
    val updatedAt: String = "",
    val fileType: NoteType = NoteType.PLAIN_TEXT
)