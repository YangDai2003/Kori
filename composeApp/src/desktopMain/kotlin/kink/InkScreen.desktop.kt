package kink

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.toBufferedImage
import java.io.File
import javax.imageio.ImageIO

@Composable
actual fun ShareImageButton(imageBitmap: ImageBitmap) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.SAVE
    ).apply {
        isVisible = false
    }
    Button(
        shape = MaterialTheme.shapes.small,
        onClick = {
            val fileName = "kori_ink_image.png"
            fileDialog.file = fileName
            fileDialog.isVisible = true
            if (fileDialog.directory != null && fileDialog.file != null) {
                val savePath = File(fileDialog.directory, fileDialog.file)
                try {
                    ImageIO.write(imageBitmap.asSkiaBitmap().toBufferedImage(), "png", savePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    ) {
        Text(stringResource(Res.string.save))
    }
}