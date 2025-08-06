package kink

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import org.yangdai.kori.presentation.util.toUIImage
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun ShareImageButton(imageBitmap: ImageBitmap) {
    Button(
        onClick = {
            imageBitmap.toUIImage()?.let { image ->
                platform.UIKit.UIImagePNGRepresentation(image).let { imageData ->
                    val tempDir = NSTemporaryDirectory()
                    val fileName = "kori_ink_image.png"
                    val path = "$tempDir/$fileName"
                    imageData?.writeToFile(path, true)
                    val fileURL = NSURL.fileURLWithPath(path)
                    val activityViewController = UIActivityViewController(listOf(fileURL), null)
                    UIApplication.sharedApplication.keyWindow?.rootViewController
                        ?.presentViewController(activityViewController, true, null)
                }
            }
        }
    ) {
        Text("Share")
    }
}