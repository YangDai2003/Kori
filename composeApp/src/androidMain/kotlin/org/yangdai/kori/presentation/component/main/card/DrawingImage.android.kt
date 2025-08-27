package org.yangdai.kori.presentation.component.main.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.screen.settings.CardSize
import java.io.File

@Composable
actual fun DrawingImage(note: NoteEntity, noteItemProperties: NoteItemProperties) {
    val context = LocalContext.current.applicationContext
    val noteDir = File(context.filesDir, note.id)
    if (!noteDir.exists()) noteDir.mkdirs()
    val imageFile = File(noteDir, "ink.png")
    val maxHeight = if (noteItemProperties.cardSize == CardSize.DEFAULT) 160.dp else 96.dp
    AsyncImage(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .fillMaxWidth()
            .heightIn(max = maxHeight),
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(imageFile)
            .addLastModifiedToFileCacheKey(true)
            .build(),
        contentScale = ContentScale.FillWidth,
        contentDescription = null
    )
}