package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import org.yangdai.kori.koriDirPathString
import java.io.File

@Composable
actual fun DrawingViewer(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier) =
    Column(modifier) {
        if (imageBitmap == null) {
            val imageFile = File("$koriDirPathString/$uuid/ink.png")
            AsyncImage(
                modifier = Modifier.fillMaxWidth(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(imageFile)
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

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }