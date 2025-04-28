package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun FilePickerDialog(onFilePicked: (PickedFile?) -> Unit) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                val path = didPickDocumentAtURL.path ?: return onFilePicked(null)
                val name = didPickDocumentAtURL.lastPathComponent
                val content = NSString.stringWithContentsOfFile(
                    path,
                    encoding = NSUTF8StringEncoding,
                    error = null
                )
                if (content != null) {
                    onFilePicked(
                        PickedFile(
                            name = name.orEmpty(),
                            path = path,
                            content = content
                        )
                    )
                } else {
                    onFilePicked(null)
                }
            }

            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                // Handle multiple URLs if needed, but we only support single file selection here
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onFilePicked(null)
            }
        }
    }

    val documentPicker = remember {
        UIDocumentPickerViewController(
            documentTypes = listOf(
                "public.text",
                "public.plain-text",
                "public.text-file",
                "public.data",
                "public.content"
            ),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen
        )
    }
    
    documentPicker.delegate = delegate
    documentPicker.allowsMultipleSelection = false

    currentUIViewController.presentViewController(
        documentPicker,
        animated = true,
        completion = null
    )
}