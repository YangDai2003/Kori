package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    val isZoomedByDefault = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    var isZoomed by remember { mutableStateOf(false) }
    var selectedSnapshot by remember { mutableStateOf(snapshots.firstOrNull()) }
    LaunchedEffect(snapshots) { selectedSnapshot = snapshots.firstOrNull() }

    selectedSnapshot?.let {
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight().weight(1f)
            ) {
                items(snapshots) { snapshot ->
                    Text(
                        text = Instant.fromEpochMilliseconds(snapshot.createdAt).toString(),
                        modifier = Modifier.fillMaxWidth().selectable(
                            selected = selectedSnapshot == snapshot,
                            onClick = { selectedSnapshot = snapshot }
                        )
                    )
                }
            }
            if (isZoomedByDefault) {
                DiffText(
                    text = currentContent,
                    oldText = it.content,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                )
            } else {
                Text(
                    text = it.content,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                )
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("No snapshots")
        }
    }
}