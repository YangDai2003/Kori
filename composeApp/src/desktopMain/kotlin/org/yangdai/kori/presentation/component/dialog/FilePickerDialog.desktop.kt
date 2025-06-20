package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import kfile.PlatformFile
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import java.io.File

@Composable
actual fun FilePickerDialog(onFilePicked: (PlatformFile?) -> Unit) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.LOAD
    ).apply {
        file = "*.txt;*.md"
        isVisible = true
    }

    if (fileDialog.file != null && fileDialog.directory != null) {
        val file = File(fileDialog.directory, fileDialog.file)
        if (file.exists() && (file.extension == "txt" || file.extension == "md" || file.extension == "markdown")) {
            onFilePicked(PlatformFile(file = file))
            return
        }
    }
    onFilePicked(null)
}

@Composable
actual fun SaveFileDialog(
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
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.SAVE
    ).apply {
        file = fileName
        isVisible = true
    }

    if (fileDialog.file != null && fileDialog.directory != null) {
        val file = File(fileDialog.directory, fileDialog.file)
        file.writeText(fileContent)
        onFileSaved(file.exists())
    } else onFileSaved(false)
}

@Composable
actual fun FilesImportDialog(onFilePicked: (List<PlatformFile>) -> Unit) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.LOAD
    ).apply {
        file = "*.txt;*.md"
        isVisible = true
        isMultipleMode = true
    }

    val selectedFiles = mutableListOf<PlatformFile>()
    if (fileDialog.files != null) {
        for (file in fileDialog.files) {
            if (file.extension == "txt" || file.extension == "md" || file.extension == "markdown") {
                selectedFiles.add(PlatformFile(file = file))
            }
        }
    }
    onFilePicked(selectedFiles)
}

@Composable
actual fun BackupJsonDialog(json: String, onJsonSaved: (Boolean) -> Unit) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.SAVE
    ).apply {
        file = "kori_backup_${Clock.System.now().toEpochMilliseconds()}.json"
        isVisible = true
    }

    if (fileDialog.file != null && fileDialog.directory != null) {
        val file = File(fileDialog.directory, fileDialog.file)
        file.writeText(json)
        onJsonSaved(file.exists())
    } else onJsonSaved(false)
}

@Composable
actual fun PickJsonDialog(onJsonPicked: (String?) -> Unit) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.LOAD
    ).apply {
        file = "*.json"
        isVisible = true
    }

    if (fileDialog.file != null && fileDialog.directory != null) {
        val file = File(fileDialog.directory, fileDialog.file)
        if (file.exists() && file.extension == "json") {
            onJsonPicked(file.readText())
            return
        }
    }
    onJsonPicked(null)
}

@Composable
actual fun PhotosPickerDialog(onPhotosPicked: (List<String>) -> Unit) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.LOAD
    ).apply {
        file = "*.jpg;*.jpeg;*.png;*.gif"
        isVisible = true
        isMultipleMode = true
    }

    val selectedPhotos = mutableListOf<String>()
    if (fileDialog.files != null) {
        for (file in fileDialog.files) {
            if (file.extension in listOf("jpg", "jpeg", "png", "webp")) {
                selectedPhotos.add(file.absolutePath)
            }
        }
    }
    onPhotosPicked(selectedPhotos)
}