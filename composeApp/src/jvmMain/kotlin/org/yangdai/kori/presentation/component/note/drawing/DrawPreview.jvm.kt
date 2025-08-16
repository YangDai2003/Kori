package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.io.File

@Composable
actual fun InNoteDrawPreview(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier) {
    if (imageBitmap == null) {
        val userHome = System.getProperty("user.home")
        val imageFile = File("$userHome/.kori/$uuid/ink.png")
        if (imageFile.exists())
            Image(
                modifier = modifier,
                bitmap = Image.makeFromEncoded(imageFile.readBytes()).toComposeImageBitmap(),
                contentDescription = null
            )
        else Spacer(modifier)
    } else {
        Image(
            modifier = modifier,
            bitmap = imageBitmap,
            contentDescription = null
        )
    }
}