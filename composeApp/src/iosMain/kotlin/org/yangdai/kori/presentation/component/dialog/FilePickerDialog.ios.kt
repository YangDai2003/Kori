package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kfile.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun FilePickerDialog(onFilePicked: (PlatformFile?) -> Unit) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                didPickDocumentAtURL.path ?: return onFilePicked(null)
                onFilePicked(PlatformFile(url = didPickDocumentAtURL))
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
        ).apply {
            this.delegate = delegate
            this.allowsMultipleSelection = false
        }
    }

    currentUIViewController.presentViewController(
        documentPicker,
        animated = true,
        completion = null
    )
}