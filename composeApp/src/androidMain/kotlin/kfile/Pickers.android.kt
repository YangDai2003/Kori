package kfile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.dialog.ExportType
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
actual fun PlatformFilePicker(onFileSelected: (PlatformFile?) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            onFileSelected(PlatformFile(context, documentUri))
        } ?: onFileSelected(null)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("text/*"))
    }
}

@Composable
actual fun NoteExporter(
    exportType: ExportType,
    noteEntity: NoteEntity,
    html: String,
    onFileSaved: (Boolean) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val mimeType = when (exportType) {
        ExportType.TXT -> "text/plain"
        ExportType.MARKDOWN -> "text/*"
        ExportType.HTML -> "text/html"
    }
    val extension = when (exportType) {
        ExportType.TXT -> ".txt"
        ExportType.MARKDOWN -> ".md"
        ExportType.HTML -> ".html"
    }
    val fileName =
        noteEntity.title.trim().replace(" ", "_").replace("/", "_").replace(":", "_") + extension
    val fileContent = if (exportType == ExportType.HTML) html else noteEntity.content
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openOutputStream(documentUri)?.bufferedWriter()
                ?.use { it.write(fileContent) }
            onFileSaved(true)
        } ?: onFileSaved(false)
    }

    LaunchedEffect(Unit) {
        saveDocumentLauncher.launch(fileName)
    }
}

@Composable
actual fun PlatformFilesPicker(onFilesSelected: (List<PlatformFile>) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val files = uris.map { uri -> PlatformFile(context, uri) }
        onFilesSelected(files)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("text/*"))
    }
}

@OptIn(ExperimentalTime::class)
@Composable
actual fun JsonExporter(json: String, onJsonSaved: (Boolean) -> Unit) {
    val context = LocalContext.current.applicationContext
    val mimeType = "application/json"
    val fileName = "kori_backup_${Clock.System.now().toEpochMilliseconds()}.json"

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openOutputStream(documentUri)?.bufferedWriter()
                ?.use { it.write(json) }
            onJsonSaved(true)
        } ?: onJsonSaved(false)
    }

    LaunchedEffect(Unit) {
        saveDocumentLauncher.launch(fileName)
    }
}

@Composable
actual fun JsonPicker(onJsonPicked: (String?) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            val json = context.contentResolver.openInputStream(documentUri)?.bufferedReader()
                ?.use { it.readText() }
            onJsonPicked(json)
        } ?: onJsonPicked(null)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("application/json"))
    }
}

@Composable
actual fun ImagesPicker(
    noteId: String,
    onImagesSelected: (List<Pair<String, String>>) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isEmpty()) {
            onImagesSelected(emptyList())
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            val savedFiles = uris.mapNotNull { uri ->
                saveFileFromUri(
                    context = context,
                    uri = uri,
                    parentDirName = noteId,
                    filePrefix = "IMG_",
                    defaultExtension = "jpg"
                )
            }
            withContext(Dispatchers.Main) {
                onImagesSelected(savedFiles)
            }
        }
    }

    LaunchedEffect(Unit) {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

@Composable
actual fun VideoPicker(
    noteId: String,
    onVideoSelected: (Pair<String, String>?) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            onVideoSelected(null)
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            val savedFile = saveFileFromUri(
                context = context,
                uri = uri,
                parentDirName = noteId,
                filePrefix = "VID_",
                defaultExtension = "mp4"
            )
            withContext(Dispatchers.Main) {
                onVideoSelected(savedFile)
            }
        }
    }

    LaunchedEffect(Unit) {
        videoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }
}

/**
 * 将给定的Uri内容保存到应用的内部存储，并在IO线程上执行。
 *
 * @param context 应用上下文
 * @param uri 内容的Uri
 * @param parentDirName 父目录名 (通常是noteId)
 * @param filePrefix 文件名前缀 (例如 "IMG_" 或 "VID_")
 * @param defaultExtension 默认扩展名 (例如 "jpg" 或 "mp4")
 * @return 一个Pair，包含最终文件名和相对路径；如果失败则返回null。
 */
private suspend fun saveFileFromUri(
    context: Context,
    uri: Uri,
    parentDirName: String,
    filePrefix: String,
    defaultExtension: String
): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        val parentDir = File(context.filesDir, parentDirName)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        // 优先获取原始文件名
        var originalFullName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    originalFullName = cursor.getString(nameIndex)
                }
            }
        }

        // 获取MIME类型对应的扩展名
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val mimeExtension =
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: defaultExtension

        val finalFileName = if (!originalFullName.isNullOrBlank()) {
            val originalExtension = originalFullName.substringAfterLast('.', "")
            if (originalExtension.isNotBlank()) {
                originalFullName
            } else {
                "$originalFullName.$mimeExtension"
            }
        } else {
            "${filePrefix}${System.currentTimeMillis()}.$mimeExtension"
        }

        val destFile = File(parentDir, finalFileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        val relativePath =
            if (parentDirName.isBlank()) finalFileName else "$parentDirName/$finalFileName"
        finalFileName to "/files/$relativePath"
    } catch (e: Exception) {
        e.printStackTrace()
        null // 操作失败时返回null
    }
}

@Composable
actual fun AudioPicker(
    noteId: String,
    onAudioSelected: (Pair<String, String>?) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            onAudioSelected(null)
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            val savedFile = saveFileFromUri(
                context = context,
                uri = uri,
                parentDirName = noteId,
                filePrefix = "ADI_",
                defaultExtension = "mp3"
            )
            withContext(Dispatchers.Main) {
                onAudioSelected(savedFile)
            }
        }
    }

    LaunchedEffect(Unit) {
        audioPicker.launch("audio/*")
    }
}