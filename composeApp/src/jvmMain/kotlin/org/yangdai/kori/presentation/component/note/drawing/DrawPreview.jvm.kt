package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import java.io.File

@Composable
actual fun DrawPreview(uuid: String, raw: String, modifier: Modifier) {
    val userHome = System.getProperty("user.home")
    val context = LocalPlatformContext.current
    key(raw) {
        AsyncImage(
            modifier = modifier,
            model = ImageRequest.Builder(context)
                .data(File("$userHome/.kori/$uuid/ink.png"))
                .addLastModifiedToFileCacheKey(true)
                .build(),
            contentDescription = null
        )
    }
}