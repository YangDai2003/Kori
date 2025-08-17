package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.export_as
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.TextOptionButton

enum class ExportType {
    TXT,
    MARKDOWN,
    HTML
}

@Composable
fun ExportDialog(
    noteEntity: NoteEntity,
    html: String,
    onDismissRequest: () -> Unit
) {
    var showSaveFileDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf(ExportType.TXT) }
    AlertDialog(
        modifier = Modifier.widthIn(max = DialogMaxWidth),
        shape = dialogShape(),
        title = { Text(stringResource(Res.string.export_as)) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                if (noteEntity.noteType == NoteType.MARKDOWN) {
                    TextOptionButton(buttonText = "MARKDOWN") {
                        exportType = ExportType.MARKDOWN
                        showSaveFileDialog = true
                    }

                    TextOptionButton(buttonText = "HTML") {
                        exportType = ExportType.HTML
                        showSaveFileDialog = true
                    }
                } else {
                    TextOptionButton(buttonText = "TXT") {
                        exportType = ExportType.TXT
                        showSaveFileDialog = true
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {}
    )

    if (showSaveFileDialog) {
        SaveFileDialog(exportType, noteEntity, html) {
            showSaveFileDialog = false
            onDismissRequest()
        }
    }
}

@Preview
@Composable
fun ExportDialogPreview() {
    ExportDialog(
        noteEntity = NoteEntity(),
        html = "",
        onDismissRequest = {}
    )
}
