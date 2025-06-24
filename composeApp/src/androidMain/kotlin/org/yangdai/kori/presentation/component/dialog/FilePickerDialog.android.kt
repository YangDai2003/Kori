package org.yangdai.kori.presentation.component.dialog

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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

@OptIn(ExperimentalUuidApi::class)
@Composable
actual fun PhotosPickerDialog(onPhotosPicked: (List<String>) -> Unit) {
    val context = LocalContext.current.applicationContext
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        val savedNames = mutableListOf<String>()
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        uris.forEach { uri ->
            val ext = context.contentResolver.getType(uri)?.substringAfterLast('/') ?: "jpg"
            val fileName = "IMG_${Uuid.random().toHexString()}.$ext"
            val destFile = File(imagesDir, fileName)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                savedNames.add(fileName)
            } catch (_: Exception) {
                // ignore failed copy
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