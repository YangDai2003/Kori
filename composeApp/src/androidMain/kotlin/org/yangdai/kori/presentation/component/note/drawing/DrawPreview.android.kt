package org.yangdai.kori.presentation.component.note.drawing

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
actual fun InNoteDrawPreview(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier) {
    if (imageBitmap == null) {
        val context = LocalContext.current.applicationContext
        val noteDir = File(context.filesDir, uuid)
        val imageFile = File(noteDir, "ink.png")
        if (imageFile.exists()) {
            Image(
                modifier = modifier,
                bitmap = BitmapFactory.decodeFile(imageFile.absolutePath).asImageBitmap(),
                contentDescription = null
            )
        } else {
            Spacer(modifier)
        }
    } else {
        Image(
            modifier = modifier,
            bitmap = imageBitmap,
            contentDescription = null
        )
    }
}