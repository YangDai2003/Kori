package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import java.io.File

@Composable
actual fun DrawPreview(uuid: String, modifier: Modifier) {
    val context = LocalPlatformContext.current
    val noteDir = File(context.filesDir, uuid)
    val imageFile = File(noteDir, "ink.png")
    val model = ImageRequest.Builder(context).data(imageFile).build()
    AsyncImage(modifier = modifier, model = model, contentDescription = null)
}