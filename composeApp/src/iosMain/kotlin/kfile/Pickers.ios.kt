package kfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.UniformTypeIdentifiers.UTTypeVideo
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
                "public.text-file"
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
    val fileName = noteEntity.title.normalizeFileName() + extension
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
actual fun PlatformFilesPicker(launch: Boolean, onFilesSelected: (List<PlatformFile>) -> Unit) {
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
                "public.text-file"
            ),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        ).apply {
            this.delegate = delegate
            this.allowsMultipleSelection = true
        }
    }

    LaunchedEffect(launch) {
        if (!launch) return@LaunchedEffect
        currentUIViewController.presentViewController(
            documentPicker,
            animated = true,
            completion = null
        )
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
@Composable
actual fun JsonExporter(launch: Boolean, json: String?, onJsonSaved: (Boolean) -> Unit) {
    val currentUIViewController = LocalUIViewController.current
    val tempDir = NSTemporaryDirectory()
    val tempDirURL = NSURL.fileURLWithPath(tempDir, isDirectory = true)
    val fileName = "kori_backup_${Clock.System.now().toEpochMilliseconds()}.json"
    val fileURL = tempDirURL.URLByAppendingPathComponent(fileName) ?: return

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

    LaunchedEffect(launch) {
        if (!launch) return@LaunchedEffect
        if (json != null) {
            val nsString = json as NSString
            val success = nsString.writeToURL(
                url = fileURL,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            if (!success) {
                onJsonSaved(false)
                return@LaunchedEffect
            }
            val documentPicker = UIDocumentPickerViewController(
                forExportingURLs = listOf(fileURL),
                asCopy = true
            ).apply {
                this.delegate = delegate
                this.allowsMultipleSelection = false
            }
            currentUIViewController.presentViewController(
                documentPicker,
                animated = true,
                completion = null
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun JsonPicker(launch: Boolean, onJsonPicked: (String?) -> Unit) {
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

    LaunchedEffect(launch) {
        if (!launch) return@LaunchedEffect
        currentUIViewController.presentViewController(
            documentPicker,
            animated = true,
            completion = null
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
actual fun ImagesPicker(
    noteId: String,
    onImagesSelected: (List<Pair<String, String>>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current

    val phpDelegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            @Suppress("UNCHECKED_CAST")
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val results = didFinishPicking as? List<PHPickerResult> ?: run {
                    onImagesSelected(emptyList())
                    return
                }
                if (results.isEmpty()) {
                    onImagesSelected(emptyList())
                    return
                }
                scope.launch(Dispatchers.IO) {
                    val imagePairs = mutableListOf<Pair<String, String>>()
                    val imageTypeIdentifier = UTTypeImage.identifier
                    for (result in results) {
                        val itemProvider = result.itemProvider
                        if (itemProvider.hasItemConformingToTypeIdentifier(imageTypeIdentifier)) {
                            val pair = suspendCancellableCoroutine { continuation ->
                                itemProvider.loadFileRepresentationForTypeIdentifier(
                                    imageTypeIdentifier
                                ) { url, error ->
                                    if (error == null && url != null) {
                                        val p = processAndCopyMediaFile(noteId, url, "image")
                                        continuation.resume(p)
                                    } else {
                                        println("Error loading file representation for image: ${error?.localizedDescription}")
                                        continuation.resume(null)
                                    }
                                }
                            }
                            pair?.let { imagePairs.add(it) }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onImagesSelected(imagePairs.toList())
                    }
                }
            }
        }
    }

    val configuration = remember {
        PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter()
            selectionLimit = 0 // 0 means multiple selection
        }
    }

    val pickerViewController = remember {
        PHPickerViewController(configuration).apply {
            delegate = phpDelegate
        }
    }

    currentUIViewController.presentViewController(
        pickerViewController,
        animated = true,
        completion = null
    )
}

@OptIn(ExperimentalTime::class)
@Composable
actual fun VideoPicker(
    noteId: String,
    onVideoSelected: (Pair<String, String>?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current

    val phpDelegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            @Suppress("UNCHECKED_CAST")
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = (didFinishPicking as? List<PHPickerResult>)?.firstOrNull()

                if (result == null) {
                    onVideoSelected(null)
                    return
                }

                val itemProvider = result.itemProvider
                val videoTypeIdentifier = UTTypeVideo.identifier
                val movieTypeIdentifier = UTTypeMovie.identifier

                if (itemProvider.hasItemConformingToTypeIdentifier(videoTypeIdentifier)) {
                    scope.launch(Dispatchers.IO) {
                        val videoPair = suspendCancellableCoroutine { continuation ->
                            itemProvider.loadFileRepresentationForTypeIdentifier(videoTypeIdentifier) { url, error ->
                                if (error == null && url != null) {
                                    val pair = processAndCopyMediaFile(noteId, url, "video")
                                    continuation.resume(pair)
                                } else {
                                    println("Error loading file representation for video: ${error?.localizedDescription}")
                                    continuation.resume(null)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            onVideoSelected(videoPair)
                        }
                    }
                } else if (itemProvider.hasItemConformingToTypeIdentifier(movieTypeIdentifier)) {
                    scope.launch(Dispatchers.IO) {
                        val videoPair = suspendCancellableCoroutine { continuation ->
                            itemProvider.loadFileRepresentationForTypeIdentifier(movieTypeIdentifier) { url, error ->
                                if (error == null && url != null) {
                                    val pair = processAndCopyMediaFile(noteId, url, "video")
                                    continuation.resume(pair)
                                } else {
                                    println("Error loading file representation for video: ${error?.localizedDescription}")
                                    continuation.resume(null)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            onVideoSelected(videoPair)
                        }
                    }
                } else {
                    onVideoSelected(null)
                }
            }
        }
    }

    val configuration = remember {
        PHPickerConfiguration().apply {
            filter = PHPickerFilter.videosFilter()
            selectionLimit = 1 // Single selection
        }
    }

    val pickerViewController = remember {
        PHPickerViewController(configuration).apply {
            delegate = phpDelegate
        }
    }

    currentUIViewController.presentViewController(
        pickerViewController,
        animated = true,
        completion = null
    )
}

@OptIn(ExperimentalTime::class)
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
    val noteDir = documentsDirectoryURL?.URLByAppendingPathComponent(noteId) ?: return null

    fileManager.createDirectoryAtURL(noteDir, true, null, null)

    val originalFileName = sourceURL.lastPathComponent ?: "${fileTypePrefix}_${
        Clock.System.now().toEpochMilliseconds()
    }.${sourceURL.pathExtension}"
    val destinationURL = noteDir.URLByAppendingPathComponent(originalFileName) ?: return null

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