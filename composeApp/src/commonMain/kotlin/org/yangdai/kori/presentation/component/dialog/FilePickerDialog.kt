package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import kfile.PlatformFile
import org.yangdai.kori.data.local.entity.NoteEntity

@Composable
expect fun FilePickerDialog(onFilePicked: (PlatformFile?) -> Unit)

@Composable
expect fun FilesImportDialog(onFilePicked: (List<PlatformFile>) -> Unit)

@Composable
expect fun BackupJsonDialog(json: String, onJsonSaved: (Boolean) -> Unit)

@Composable
expect fun PickJsonDialog(onJsonPicked: (String?) -> Unit)

@Composable
expect fun SaveFileDialog(
    exportType: ExportType,
    noteEntity: NoteEntity,
    html: String,
    onFileSaved: (Boolean) -> Unit
)

@Composable
expect fun PhotosPickerDialog(noteId: String, onPhotosPicked: (List<Pair<String, String>>) -> Unit)