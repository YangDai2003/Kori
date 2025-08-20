package kfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.dialog.ExportType
import org.yangdai.kori.presentation.component.note.markdown.IOS_CUSTOM_SCHEME
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformFilePicker(onFileSelected: (PlatformFile?) -> Unit) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                onFileSelected(PlatformFile(url = didPickDocumentAtURL))
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onFileSelected(null)
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

@OptIn(ExperimentalForeignApi::class)
@Suppress("CAST_NEVER_SUCCEEDS")
@Composable
actual fun NoteExporter(
    exportType: ExportType,
    noteEntity: NoteEntity,
    html: String,
    onFileSaved: (Boolean) -> Unit
) {
    val extension = when (exportType) {
        ExportType.TXT -> ".txt"
        ExportType.MARKDOWN -> ".md"
        ExportType.HTML -> ".html"
    }
    val fileName =
        noteEntity.title.trim().replace(" ", "_").replace("/", "_").replace(":", "_") + extension
    val fileContent = if (exportType == ExportType.HTML) html else noteEntity.content

    val currentUIViewController = LocalUIViewController.current
    val tempDir = NSTemporaryDirectory()
    val tempDirURL = NSURL.fileURLWithPath(tempDir, isDirectory = true)
    val fileURL = tempDirURL.URLByAppendingPathComponent(fileName) ?: return
    val nsString = fileContent as NSString
    val success = nsString.writeToURL(
        url = fileURL,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )
    if (!success) return
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                onFileSaved(true)
                NSFileManager.defaultManager.removeItemAtURL(fileURL, null)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onFileSaved(false)
                NSFileManager.defaultManager.removeItemAtURL(fileURL, null)
            }
        }
    }
    val documentPicker = remember {
        UIDocumentPickerViewController(
            forExportingURLs = listOf(fileURL),
            asCopy = true
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

@Composable
actual fun PlatformFilesPicker(onFilesSelected: (List<PlatformFile>) -> Unit) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val files = didPickDocumentsAtURLs.mapNotNull { url ->
                    (url as? NSURL)?.let { PlatformFile(url = it) }
                }
                onFilesSelected(files)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onFilesSelected(emptyList())
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
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        ).apply {
            this.delegate = delegate
            this.allowsMultipleSelection = true
        }
    }

    currentUIViewController.presentViewController(
        documentPicker,
        animated = true,
        completion = null
    )
}

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
@Composable
actual fun JsonExporter(json: String, onJsonSaved: (Boolean) -> Unit) {
    val currentUIViewController = LocalUIViewController.current
    val tempDir = NSTemporaryDirectory()
    val tempDirURL = NSURL.fileURLWithPath(tempDir, isDirectory = true)
    val fileName = "kori_backup_${Clock.System.now().toEpochMilliseconds()}.json"
    val fileURL = tempDirURL.URLByAppendingPathComponent(fileName) ?: return
    val nsString = json as NSString
    val success = nsString.writeToURL(
        url = fileURL,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )
    if (!success) return
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                onJsonSaved(true)
                NSFileManager.defaultManager.removeItemAtURL(fileURL, null)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onJsonSaved(false)
                NSFileManager.defaultManager.removeItemAtURL(fileURL, null)
            }
        }
    }
    val documentPicker = remember {
        UIDocumentPickerViewController(
            forExportingURLs = listOf(fileURL),
            asCopy = true
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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun JsonPicker(onJsonPicked: (String?) -> Unit) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                val json = NSString.stringWithContentsOfURL(
                    didPickDocumentAtURL,
                    NSUTF8StringEncoding,
                    null
                )
                onJsonPicked(json)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onJsonPicked(null)
            }
        }
    }

    val documentPicker = remember {
        UIDocumentPickerViewController(
            documentTypes = listOf("public.json"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
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

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
@Composable
actual fun ImagesPicker(
    noteId: String,
    onImagesSelected: (List<Pair<String, String>>) -> Unit
) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val newImagePairs = mutableListOf<Pair<String, String>>()
                for (imageURLObject in didPickDocumentsAtURLs) {
                    val sourceURL = imageURLObject as? NSURL ?: continue
                    processAndCopyMediaFile(noteId, sourceURL, "image")?.let {
                        newImagePairs.add(it)
                    }
                }
                onImagesSelected(newImagePairs)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onImagesSelected(emptyList())
            }
        }
    }

    val documentPicker = remember {
        UIDocumentPickerViewController(
            documentTypes = listOf("public.image"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        ).apply {
            this.delegate = delegate
            this.allowsMultipleSelection = true
        }
    }

    currentUIViewController.presentViewController(
        documentPicker,
        animated = true,
        completion = null
    )
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
@Composable
actual fun VideoPicker(
    noteId: String,
    onVideoSelected: (Pair<String, String>?) -> Unit
) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                val result = processAndCopyMediaFile(noteId, didPickDocumentAtURL, "video")
                onVideoSelected(result)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onVideoSelected(null)
            }
        }
    }

    val documentPicker = remember {
        UIDocumentPickerViewController(
            documentTypes = listOf("public.movie", "public.video"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
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

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
@Composable
actual fun AudioPicker(
    noteId: String,
    onAudioSelected: (Pair<String, String>?) -> Unit
) {
    val currentUIViewController = LocalUIViewController.current

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                val result = processAndCopyMediaFile(noteId, didPickDocumentAtURL, "audio")
                onAudioSelected(result)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onAudioSelected(null)
            }
        }
    }

    val documentPicker = remember {
        UIDocumentPickerViewController(
            documentTypes = listOf("public.audio"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
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

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun processAndCopyMediaFile(
    noteId: String,
    sourceURL: NSURL,
    fileTypePrefix: String
): Pair<String, String>? {
    val fileManager = NSFileManager.defaultManager
    val documentsDirectoryURL =
        fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).firstOrNull() as? NSURL
    val noteDir = documentsDirectoryURL?.URLByAppendingPathComponent(noteId)

    if (noteDir == null) {
        return null
    }
    fileManager.createDirectoryAtURL(noteDir, true, null, null)

    val originalFileName = sourceURL.lastPathComponent ?: "${fileTypePrefix}_${
        Clock.System.now().toEpochMilliseconds()
    }.${sourceURL.pathExtension}"
    val destinationURL = noteDir.URLByAppendingPathComponent(originalFileName)

    if (destinationURL == null) {
        return null
    }

    if (fileManager.fileExistsAtPath(destinationURL.path!!)) {
        fileManager.removeItemAtURL(destinationURL, null)
    }

    val success = fileManager.copyItemAtURL(sourceURL, destinationURL, null)
    return if (success) {
        val relativePath = if (noteId.isBlank()) originalFileName else "$noteId/$originalFileName"
        Pair(originalFileName, "$IOS_CUSTOM_SCHEME://$relativePath")
    } else {
        null
    }
}