package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.destination_folder
import kori.composeapp.generated.resources.move
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.FolderEntity

@Composable
fun FoldersDialog(
    hint: String = stringResource(Res.string.destination_folder),
    oFolderId: String?,
    foldersWithNoteCounts: List<FolderDao.FolderWithNoteCount>,
    onDismissRequest: () -> Unit,
    onSelect: (String?) -> Unit
) {

    var selectedFolderId by remember { mutableStateOf(oFolderId) }

    AlertDialog(
        title = { Text(text = hint) },
        text = {
            Column {
                HorizontalDivider(Modifier.fillMaxWidth())
                LazyColumn(modifier = Modifier.fillMaxWidth()) {

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedFolderId = null
                            }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(start = 0.dp, end = 16.dp)
                                    .padding(vertical = 16.dp),
                                selected = null == selectedFolderId,
                                onClick = null
                            )

                            Icon(
                                tint = MaterialTheme.colorScheme.onSurface,
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null
                            )

                            Text(
                                text = stringResource(Res.string.all_notes),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                    items(foldersWithNoteCounts) { group ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedFolderId = group.folder.id
                            }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(start = 0.dp, end = 16.dp)
                                    .padding(vertical = 16.dp),
                                selected = group.folder.id == selectedFolderId,
                                onClick = null
                            )

                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                tint = if (group.folder.colorValue != FolderEntity.defaultColorValue) Color(
                                    group.folder.colorValue
                                ) else MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )

                            Text(
                                text = group.folder.name,
                                modifier = Modifier.padding(start = 16.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth())
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onSelect(selectedFolderId)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(Res.string.move))
            }
        })
}