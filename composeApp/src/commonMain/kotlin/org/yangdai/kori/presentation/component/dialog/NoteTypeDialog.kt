package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.todo_text
import kori.composeapp.generated.resources.type
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.yangdai.kori.data.local.entity.NoteType

@Preview
@Composable
fun NoteTypeDialogPreview() {
    NoteTypeDialog(
        oNoteType = NoteType.PLAIN_TEXT,
        onDismissRequest = {},
        onNoteTypeSelected = {}
    )
}

@Composable
fun NoteTypeDialog(
    oNoteType: NoteType,
    onDismissRequest: () -> Unit,
    onNoteTypeSelected: (NoteType) -> Unit
) {
    var selectedNoteType by remember { mutableStateOf(oNoteType) }
    AlertDialog(
        modifier = Modifier.widthIn(max = DialogMaxWidth),
        onDismissRequest = onDismissRequest,
        shape = dialogShape(),
        title = { Text(stringResource(Res.string.type)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                NoteType.entries.filter { it != NoteType.Drawing }.forEach { noteType ->
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .selectable(
                                selected = (selectedNoteType == noteType),
                                role = Role.RadioButton,
                                onClick = {
                                    selectedNoteType = noteType
                                    onNoteTypeSelected(noteType)
                                    onDismissRequest()
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val typeName = when (noteType) {
                            NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                            NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                            NoteType.TODO -> stringResource(Res.string.todo_text)
                            NoteType.Drawing -> stringResource(Res.string.drawing)
                        }
                        Text(
                            typeName,
                            modifier = Modifier.minimumInteractiveComponentSize()
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        RadioButton(
                            modifier = Modifier.padding(end = 16.dp),
                            selected = (selectedNoteType == noteType),
                            onClick = null
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}