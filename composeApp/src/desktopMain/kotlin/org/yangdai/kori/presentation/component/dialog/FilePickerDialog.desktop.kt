package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import kfile.PlatformFile
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalUuidApi::class)
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

    val savedNames = mutableListOf<String>()
    if (fileDialog.files != null) {
        val userHome: String = System.getProperty("user.home")
        val imagesDir = File(File(userHome, ".kori"), "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        for (file in fileDialog.files) {
            if (file.extension in listOf("jpg", "jpeg", "png", "webp", "gif")) {
                val ext = file.extension.ifBlank { "jpg" }
                val fileName = "IMG_${Uuid.random().toHexString()}.$ext"
                val destFile = File(imagesDir, fileName)
                try {
                    Files.copy(
                        file.toPath(),
                        destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                    savedNames.add(fileName)
                } catch (_: Exception) {
                    // ignore failed copy
                }
            }
        }
    }
    onPhotosPicked(savedNames)
}