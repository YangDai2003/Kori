package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowSizeClass
import kotlinx.datetime.Instant
import org.yangdai.kori.data.local.entity.SnapshotEntity
import org.yangdai.kori.presentation.component.note.DiffText

@Composable
fun SnapshotsDialog(
    snapshots: List<SnapshotEntity>,
    currentContent: String,
    onDismissRequest: () -> Unit,
    onRestoreSnapshot: (SnapshotEntity) -> Unit,
    onDeleteSnapshot: (SnapshotEntity) -> Unit,
    onClearSnapshots: () -> Unit
) = Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    )
) {
    Surface {
        SnapshotsDialogContent(
            snapshots = snapshots,
            currentContent = currentContent,
            onDismissRequest = onDismissRequest,
            onRestoreSnapshot = onRestoreSnapshot,
            onDeleteSnapshot = onDeleteSnapshot,
            onClearSnapshots = onClearSnapshots
        )
    }
}

@Composable
private fun SnapshotsDialogContent(
    snapshots: List<SnapshotEntity>,
    currentContent: String,
    onDismissRequest: () -> Unit,
    onRestoreSnapshot: (SnapshotEntity) -> Unit,
    onDeleteSnapshot: (SnapshotEntity) -> Unit,
    onClearSnapshots: () -> Unit
) {
    val isWideScreen = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    var selectedSnapshot by remember { mutableStateOf(snapshots.firstOrNull()) }
    LaunchedEffect(snapshots) { selectedSnapshot = snapshots.firstOrNull() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with title and actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Snapshots",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        if (snapshots.isNotEmpty()) {
            if (isWideScreen) {
                // Wide screen: show sidebar and content side by side
                Row(modifier = Modifier.weight(1f)) {
                    // Sidebar with snapshot list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.3f)
                            .padding(16.dp)
                    ) {
                        items(snapshots) { snapshot ->
                            SnapshotListItem(
                                snapshot = snapshot,
                                isSelected = selectedSnapshot?.id == snapshot.id,
                                onSelect = { selectedSnapshot = snapshot },
                                onRestore = { onRestoreSnapshot(snapshot) },
                                onDelete = { onDeleteSnapshot(snapshot) }
                            )
                        }

                        if (snapshots.size > 1)
                            item {
                                // Clear all button
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FilledTonalButton(
                                        onClick = onClearSnapshots,
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear All")
                                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                        Text("Clear All Snapshots")
                                    }
                                }
                            }
                    }

                    // Divider
                    VerticalDivider()

                    // Main content area
                    selectedSnapshot?.let { snapshot ->
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.7f)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Comparison: Current vs Selected Snapshot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DiffText(
                                text = currentContent,
                                oldText = snapshot.content,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onDeleteSnapshot(snapshot) },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text("Delete")
                                }

                                Button(
                                    onClick = { onRestoreSnapshot(snapshot) }
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Restore")
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text("Restore")
                                }
                            }
                        }
                    }
                }
            } else {
                // Narrow screen: show snapshot list with bottom sheet for details
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(snapshots) { snapshot ->
                        SnapshotListItem(
                            snapshot = snapshot,
                            isSelected = selectedSnapshot?.id == snapshot.id,
                            onSelect = { selectedSnapshot = snapshot },
                            onRestore = { onRestoreSnapshot(snapshot) },
                            onDelete = { onDeleteSnapshot(snapshot) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Show selected snapshot content in a card below the list
                selectedSnapshot?.let { snapshot ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Selected Snapshot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = snapshot.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onDeleteSnapshot(snapshot) },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text("Delete")
                                }

                                Button(
                                    onClick = { onRestoreSnapshot(snapshot) }
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Restore")
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text("Restore")
                                }
                            }
                        }
                    }
                }

                // Clear all button
                if (snapshots.size > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            onClick = onClearSnapshots,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All")
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Clear All Snapshots")
                        }
                    }
                }
            }
        } else {
            // No snapshots message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No Snapshots Available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Create some content to automatically save snapshots",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotListItem(
    snapshot: SnapshotEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) = ListItem(
    headlineContent = {
        Text(
            text = Instant.fromEpochMilliseconds(snapshot.createdAt).toString(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    supportingContent = {
        Text(
            text = "Snapshot #${snapshot.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    },
    trailingContent = {
        var expanded by remember { mutableStateOf(false) }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
        }
        // Dropdown menu for actions
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Restore") },
                onClick = {
                    onRestore()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onDelete()
                    expanded = false
                }
            )
        }
    },
    modifier = Modifier
        .fillMaxWidth()
        .selectable(
            selected = isSelected,
            onClick = onSelect
        )
        .padding(vertical = 4.dp),
    colors = if (isSelected) ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) else ListItemDefaults.colors(
        containerColor = Color.Transparent
    )
)