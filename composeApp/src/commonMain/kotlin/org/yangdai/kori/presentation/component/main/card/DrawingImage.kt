package org.yangdai.kori.presentation.component.main.card

import androidx.compose.runtime.Composable
import org.yangdai.kori.data.local.entity.NoteEntity

@Composable
expect fun DrawingImage(note: NoteEntity, noteItemProperties: NoteItemProperties)