package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.ImageBitmap
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.share
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.util.toUIImage
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SaveBitmapToFileOnDispose(imageBitmap: ImageBitmap?, uuid: String) {
    DisposableEffect(uuid, imageBitmap) {
        onDispose {
            imageBitmap?.toUIImage()?.let { image ->
                platform.UIKit.UIImagePNGRepresentation(image)?.let { imageData ->
                    val documentDirectory = NSSearchPathForDirectoriesInDomains(
                        platform.Foundation.NSDocumentDirectory,
                        platform.Foundation.NSUserDomainMask,
                        true
                    ).firstOrNull() as? String
                    documentDirectory?.let {
                        val filePath = "$it/$uuid/ink.png"
                        // Ensure the directory exists
                        NSFileManager.defaultManager
                            .createDirectoryAtPath("$it/$uuid", true, null, null)
                        imageData.writeToFile(filePath, true)
                    }
                }
            }
        }
    }
}