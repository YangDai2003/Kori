package kfile

import androidx.compose.runtime.Composable
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.dialog.ExportType
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
actual fun PlatformFilePicker(onFileSelected: (PlatformFile?) -> Unit) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.LOAD
    ).apply {
        file = "*.txt;*.md"
        isVisible = true
    }

    if (fileDialog.file != null && fileDialog.directory != null) {
        val file = File(fileDialog.directory, fileDialog.file)
        if (file.exists() && (file.extension == "txt" || file.extension == "md" || file.extension == "markdown")) {
            onFileSelected(PlatformFile(file = file))
            return
        }
    }
    onFileSelected(null)
}

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
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.SAVE
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
actual fun PlatformFilesPicker(onFilesSelected: (List<PlatformFile>) -> Unit) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.LOAD
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
    onFilesSelected(selectedFiles)
}

@OptIn(ExperimentalTime::class)
@Composable
actual fun JsonExporter(json: String, onJsonSaved: (Boolean) -> Unit) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.SAVE
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
actual fun JsonPicker(onJsonPicked: (String?) -> Unit) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.LOAD
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
actual fun ImagesPicker(
    noteId: String,
    onImagesSelected: (List<Pair<String, String>>) -> Unit
) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.LOAD
    ).apply {
        file = "*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.webp"
        isVisible = true
        isMultipleMode = true
    }

    val savedNames = mutableListOf<Pair<String, String>>()
    if (fileDialog.files != null && fileDialog.files.isNotEmpty()) {

        val userHome: String = System.getProperty("user.home")
        val imagesDir = File(File(userHome, ".kori"), noteId)
        if (!imagesDir.exists()) imagesDir.mkdirs()

        for (file in fileDialog.files) {
            if (file.extension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")) {
                val destFile = File(imagesDir, file.name)
                try {
                    Files.copy(
                        file.toPath(),
                        destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                    savedNames.add(
                        destFile.name to
                                "file:///${destFile.absolutePath.replace("\\", "/")}"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    onImagesSelected(savedNames)
}

@Composable
actual fun VideoPicker(
    noteId: String,
    onVideoSelected: (Pair<String, String>?) -> Unit
) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.LOAD
    ).apply {
        file = "*.mp4;*.avi;*.mkv;*.mov;*.wmv;*.flv;*.webm"
        isVisible = true
        isMultipleMode = false
    }

    var savedName: Pair<String, String>? = null
    if (fileDialog.file != null && fileDialog.directory != null) {
        val userHome: String = System.getProperty("user.home")
        val videoDir = File(File(userHome, ".kori"), noteId)
        if (!videoDir.exists()) videoDir.mkdirs()
        val file = File(fileDialog.directory, fileDialog.file)
        if (file.extension in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")) {
            val destFile = File(videoDir, file.name)
            try {
                Files.copy(
                    file.toPath(),
                    destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                savedName = destFile.name to
                        "file:///${destFile.absolutePath.replace("\\", "/")}"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    onVideoSelected(savedName)
}

@Composable
actual fun AudioPicker(
    noteId: String,
    onAudioSelected: (Pair<String, String>?) -> Unit
) {
    val fileDialog = FileDialog(
        null as Frame?,
        stringResource(Res.string.app_name),
        FileDialog.LOAD
    ).apply {
        file = "*.mp3;*.wav;*.ogg;*.aac;*.flac;*.m4a"
        isVisible = true
        isMultipleMode = false
    }

    var savedName: Pair<String, String>? = null
    if (fileDialog.file != null && fileDialog.directory != null) {
        val userHome: String = System.getProperty("user.home")
        val audioDir = File(File(userHome, ".kori"), noteId)
        if (!audioDir.exists()) audioDir.mkdirs()
        val file = File(fileDialog.directory, fileDialog.file)
        if (file.extension in listOf("mp3", "wav", "ogg", "aac", "flac", "m4a")) {
            val destFile = File(audioDir, file.name)
            try {
                Files.copy(
                    file.toPath(),
                    destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                savedName = destFile.name to "file:///${destFile.absolutePath.replace("\\", "/")}"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    onAudioSelected(savedName)
}