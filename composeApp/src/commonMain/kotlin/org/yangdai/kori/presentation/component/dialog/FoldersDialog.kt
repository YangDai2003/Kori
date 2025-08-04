package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.destination_folder
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.defaultFolderColor

@Composable
fun FoldersDialog(
    hint: String = stringResource(Res.string.destination_folder),
    oFolderId: String?,
    foldersWithNoteCounts: List<FolderDao.FolderWithNoteCount>,
    onDismissRequest: () -> Unit,
    onSelect: (String?) -> Unit
) {

    var selectedFolderId by remember { mutableStateOf(oFolderId) }
    val haptic = LocalHapticFeedback.current

    val onConfirm = {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        onSelect(selectedFolderId)
        onDismissRequest()
    }

    AlertDialog(
        modifier = Modifier.widthIn(max = DialogMaxWidth),
        shape = dialogShape(),
        title = { Text(hint) },
        text = {
            Column {
                HorizontalDivider(Modifier.fillMaxWidth())
                LazyColumn(
                    Modifier.fillMaxWidth().onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyUp && it.key == Key.Enter && it.isShiftPressed) {
                            onConfirm()
                            true
                        } else {
                            false
                        }
                    }
                ) {
                    item {
                        val isSelected = selectedFolderId == null
                        ListItem(
                            modifier = Modifier.clickable { selectedFolderId = null },
                            leadingContent = {
                                Icon(
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    imageVector = if (isSelected) Icons.Outlined.FolderOpen
                                    else Icons.Outlined.Folder,
                                    contentDescription = null
                                )
                            },
                            headlineContent = {
                                Text(stringResource(Res.string.all_notes))
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    items(foldersWithNoteCounts, key = { it.folder.id }) { group ->
                        val folder = group.folder
                        val isSelected = selectedFolderId == folder.id
                        ListItem(
                            modifier = Modifier.clickable { selectedFolderId = folder.id },
                            leadingContent = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Outlined.FolderOpen
                                    else Icons.Outlined.Folder,
                                    tint = if (folder.colorValue != defaultFolderColor)
                                        Color(folder.colorValue)
                                    else MaterialTheme.colorScheme.onSurface,
                                    contentDescription = null
                                )
                            },
                            headlineContent = {
                                Text(
                                    folder.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth())
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = { DismissButton(onDismissRequest) },
        confirmButton = { ConfirmButton(onClick = onConfirm) }
    )
}

@Composable
@Preview
fun FoldersDialogPreview() {
    FoldersDialog(
        hint = stringResource(Res.string.destination_folder),
        oFolderId = null,
        foldersWithNoteCounts = listOf(),
        onDismissRequest = {},
        onSelect = {}
    )
}