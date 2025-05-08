package org.yangdai.kori.presentation.screen.file

import org.yangdai.kori.data.local.entity.NoteType

data class FileEditingState(
    val noteType: NoteType = NoteType.PLAIN_TEXT
)