package org.yangdai.kori.presentation.component.dialog

import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kfile.PlatformFile
import org.yangdai.kori.data.local.entity.NoteEntity
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
actual fun FilePickerDialog(onFilePicked: (PlatformFile?) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            onFilePicked(PlatformFile(context, documentUri))
        } ?: onFilePicked(null)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("text/*"))
    }
}

@Composable
actual fun SaveFileDialog(
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
actual fun FilesImportDialog(onFilePicked: (List<PlatformFile>) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val files = uris.map { uri -> PlatformFile(context, uri) }
        onFilePicked(files)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("text/*"))
    }
}

@OptIn(ExperimentalTime::class)
@Composable
actual fun BackupJsonDialog(json: String, onJsonSaved: (Boolean) -> Unit) {
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
actual fun PickJsonDialog(onJsonPicked: (String?) -> Unit) {
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
actual fun PhotosPickerDialog(
    noteId: String,
    onPhotosPicked: (List<Pair<String, String>>) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isEmpty()) {
            onPhotosPicked(emptyList())
            return@rememberLauncherForActivityResult
        }

        val savedNames = mutableListOf<Pair<String, String>>()
        val imagesDir = File(context.filesDir, noteId)
        if (!imagesDir.exists()) imagesDir.mkdirs()

        uris.forEach { uri ->
            try {
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

                // 获取基于MIME类型的备用扩展名，以备后用
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val mimeExtension =
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

                val finalFileName = if (!originalFullName.isNullOrBlank()) {
                    // 检查原始文件名是否包含一个点以及点后面的字符
                    val originalExtension = originalFullName.substringAfterLast('.', "")
                    if (originalExtension.isNotBlank()) {
                        // --- 情况 A: 原始文件名已包含扩展名 ---
                        // 直接使用，例如 "IMG_1234.JPG" -> "IMG_1234.JPG"
                        originalFullName
                    } else {
                        // --- 情况 B: 原始文件名不含扩展名 ---
                        // 将原始名称和MIME扩展名结合，例如 "MyPhoto" -> "MyPhoto.png"
                        "$originalFullName.$mimeExtension"
                    }
                } else {
                    // --- 情况 C: 无法获取任何原始文件名 ---
                    // 生成一个全新的文件名，例如 "IMG_1692081000000.jpg"
                    "IMG_${System.currentTimeMillis()}.$mimeExtension"
                }

                val destFile = File(imagesDir, finalFileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val relativePath = if (noteId.isBlank()) finalFileName else "$noteId/$finalFileName"
                savedNames.add(finalFileName to "/files/$relativePath")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onPhotosPicked(savedNames)
    }

    LaunchedEffect(Unit) {
        photoPicker.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
}