package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.type
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTypeDialog(
    oNoteType: NoteType,
    onDismissRequest: () -> Unit,
    onNoteTypeSelected: (NoteType) -> Unit
) {
    var selectedNoteType by remember { mutableStateOf(oNoteType) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.type)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                NoteType.entries.forEach { noteType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (selectedNoteType == noteType),
                                role = Role.RadioButton,
                                onClick = {
                                    selectedNoteType = noteType
                                    onNoteTypeSelected(noteType)
                                    onDismissRequest()
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(end = 16.dp),
                            selected = (selectedNoteType == noteType),
                            onClick = null
                        )
                        val typeName = when (noteType) {
                            NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                            NoteType.LITE_MARKDOWN -> stringResource(Res.string.markdown) + " (Lite)"
                            NoteType.STANDARD_MARKDOWN -> stringResource(Res.string.markdown) + " (Standard)"
                        }
                        Text(typeName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}