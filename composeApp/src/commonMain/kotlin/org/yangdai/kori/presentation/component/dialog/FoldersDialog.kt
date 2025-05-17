package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.confirm
import kori.composeapp.generated.resources.destination_folder
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.defaultFolderColor
import org.yangdai.kori.presentation.component.VerticalLazyListScrollbar

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
        title = { Text(hint) },
        text = {
            Column {
                HorizontalDivider(Modifier.fillMaxWidth())
                Box(Modifier.fillMaxWidth()) {
                    val state = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxWidth(), state) {
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
                    VerticalLazyListScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        state = state
                    )
                }
                HorizontalDivider(Modifier.fillMaxWidth())
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onDismissRequest) {
                Text(stringResource(Res.string.cancel))
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
                Text(stringResource(Res.string.confirm))
            }
        })
}