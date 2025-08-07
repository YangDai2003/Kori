package kink

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.share
import org.jetbrains.compose.resources.stringResource
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
                platform.UIKit.UIImagePNGRepresentation(image)?.let { imageData ->
                    val tempDir = NSTemporaryDirectory()
                    val tempDirURL = NSURL.fileURLWithPath(tempDir, isDirectory = true)
                    val fileName = "kori_ink_image.png"
                    tempDirURL.URLByAppendingPathComponent(fileName)?.path?.let { filePath ->
                        imageData.writeToFile(filePath, true)
                        NSURL.fileURLWithPath(filePath).let { fileURL ->
                            val activityViewController =
                                UIActivityViewController(listOf(fileURL), null)
                            var currentViewController =
                                UIApplication.sharedApplication.keyWindow?.rootViewController
                            while (currentViewController?.presentedViewController != null) {
                                currentViewController =
                                    currentViewController.presentedViewController
                            }
                            currentViewController?.presentViewController(
                                viewControllerToPresent = activityViewController,
                                animated = true,
                                completion = null
                            )
                        }
                    }
                }
            }
        }
    ) {
        Text(stringResource(Res.string.share))
    }
}