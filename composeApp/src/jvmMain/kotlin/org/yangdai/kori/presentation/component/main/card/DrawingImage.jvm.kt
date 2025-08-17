package org.yangdai.kori.presentation.component.main.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
    val userHome = System.getProperty("user.home")
    val imageFile = File("$userHome/.kori/${note.id}/ink.png")
    val maxHeight = if (noteItemProperties.cardSize == CardSize.DEFAULT) 240.dp else 160.dp
    AsyncImage(
        modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight),
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(imageFile)
            .addLastModifiedToFileCacheKey(true)
            .build(),
        contentScale = ContentScale.FillWidth,
        contentDescription = null
    )
}