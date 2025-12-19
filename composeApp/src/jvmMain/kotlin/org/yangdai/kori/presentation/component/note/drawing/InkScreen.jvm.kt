package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toAwtImage
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.save
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.toBufferedImage
import org.yangdai.kori.koriDirPath
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

@Composable
actual fun ShareImageButton(imageBitmap: ImageBitmap) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.SAVE
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
                val savePath = Paths.get(fileDialog.directory, fileDialog.file).toFile()
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

@Composable
actual fun SaveBitmapToFileOnDispose(imageBitmap: ImageBitmap?, uuid: String) {
    DisposableEffect(uuid, imageBitmap) {
        onDispose {
            if (uuid.isEmpty() || imageBitmap == null) return@onDispose
            CoroutineScope(Dispatchers.IO).launch {
                val noteDirectoryPath = koriDirPath.resolve(uuid)
                if (!Files.exists(noteDirectoryPath)) Files.createDirectories(noteDirectoryPath)
                val bitmapFilePath = noteDirectoryPath.resolve("ink.png")
                Files.newOutputStream(bitmapFilePath).use { outputStream ->
                    ImageIO.write(imageBitmap.toAwtImage(), "png", outputStream)
                }
            }
        }
    }
}