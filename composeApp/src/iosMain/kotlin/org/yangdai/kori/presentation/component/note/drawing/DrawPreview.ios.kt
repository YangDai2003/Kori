package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.viewinterop.UIKitView
import org.yangdai.kori.presentation.util.toUIColor
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

@Composable
actual fun InNoteDrawPreview(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier) =
    Column(modifier) {
        if (imageBitmap == null) {
            val backgroundColor = MaterialTheme.colorScheme.background
            UIKitView(
                modifier = Modifier.fillMaxWidth(),
                factory = {
                    UIImageView().apply {
                        contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                        this.backgroundColor = backgroundColor.toUIColor()
                    }
                },
                update = { imageView ->
                    val documentDirectory = NSFileManager.defaultManager.URLsForDirectory(
                        platform.Foundation.NSDocumentDirectory,
                        platform.Foundation.NSUserDomainMask
                    ).first() as NSURL
                    val fileManager = NSFileManager.defaultManager
                    val imagePath =
                        documentDirectory.URLByAppendingPathComponent("$uuid/ink.png")?.path
                    if (imagePath != null && fileManager.fileExistsAtPath(imagePath)) {
                        UIImage.imageWithContentsOfFile(imagePath)
                    } else {
                        null
                    }?.also {
                        imageView.image = it
                    }
                }
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