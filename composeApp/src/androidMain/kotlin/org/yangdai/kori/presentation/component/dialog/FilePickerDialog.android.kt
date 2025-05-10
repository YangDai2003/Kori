package org.yangdai.kori.presentation.component.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kfile.PlatformFile
import org.yangdai.kori.data.local.entity.NoteEntity

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