package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

@Composable
actual fun DrawingViewer(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier) =
    Column(modifier) {
        if (imageBitmap == null) {
            val documentDirectory = NSFileManager.defaultManager.URLsForDirectory(
                platform.Foundation.NSDocumentDirectory,
                platform.Foundation.NSUserDomainMask
            ).firstOrNull() as? NSURL
            val imagePath = documentDirectory?.URLByAppendingPathComponent("$uuid/ink.png")?.path
            AsyncImage(
                modifier = Modifier.fillMaxWidth(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(imagePath)
                    .addLastModifiedToFileCacheKey(true)
                    .build(),
                contentScale = ContentScale.FillWidth,
                contentDescription = null
            )
        } else {
            Image(
                modifier = Modifier.fillMaxWidth(),
                bitmap = imageBitmap,
                contentDescription = null
            )
        }

        Spacer(Modifier.navigationBarsPadding())
    }