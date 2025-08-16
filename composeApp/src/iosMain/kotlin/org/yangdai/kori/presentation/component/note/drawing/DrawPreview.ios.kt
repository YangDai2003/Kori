package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
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
actual fun InNoteDrawPreview(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier) {
    if (imageBitmap == null) {
        val documentDirectory = NSFileManager.defaultManager.URLsForDirectory(
            platform.Foundation.NSDocumentDirectory,
            platform.Foundation.NSUserDomainMask
        ).first() as NSURL
        val imagePath = documentDirectory.URLByAppendingPathComponent("$uuid/ink.png")?.path
        val image = imagePath?.let { UIImage.imageWithContentsOfFile(it) }
        val backgroundColor = MaterialTheme.colorScheme.background
        if (image != null)
            UIKitView(
                modifier = modifier,
                factory = {
                    UIImageView().apply {
                        this.image = image
                        contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                        this.backgroundColor = backgroundColor.toUIColor()
                    }
                }
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