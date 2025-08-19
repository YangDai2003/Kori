package kfile

import androidx.compose.runtime.Composable
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.dialog.ExportType

@Composable
expect fun PlatformFilePicker(onFileSelected: (PlatformFile?) -> Unit)

@Composable
expect fun PlatformFilesPicker(onFilesSelected: (List<PlatformFile>) -> Unit)

@Composable
expect fun JsonExporter(json: String, onJsonSaved: (Boolean) -> Unit)

@Composable
expect fun JsonPicker(onJsonPicked: (String?) -> Unit)

@Composable
expect fun NoteExporter(
    exportType: ExportType,
    noteEntity: NoteEntity,
    html: String,
    onFileSaved: (Boolean) -> Unit
)

@Composable
expect fun ImagesPicker(noteId: String, onImagesSelected: (List<Pair<String, String>>) -> Unit)

@Composable
expect fun VideoPicker(noteId: String, onVideoSelected: (Pair<String, String>?) -> Unit)

@Composable
expect fun AudioPicker(noteId: String, onAudioSelected: (Pair<String, String>?) -> Unit)