package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Difference
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.delete_all
import kori.composeapp.generated.resources.no_snapshots
import kori.composeapp.generated.resources.restore
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.SnapshotEntity
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.note.DiffText
import org.yangdai.kori.presentation.util.formatNumber

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
    SnapshotsDialogContent(
        snapshots = snapshots,
        currentContent = currentContent,
        onDismissRequest = onDismissRequest,
        onRestoreSnapshot = onRestoreSnapshot,
        onDeleteSnapshot = onDeleteSnapshot,
        onClearSnapshots = onClearSnapshots
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SnapshotsDialogContent(
    snapshots: List<SnapshotEntity>,
    currentContent: String,
    onDismissRequest: () -> Unit,
    onRestoreSnapshot: (SnapshotEntity) -> Unit,
    onDeleteSnapshot: (SnapshotEntity) -> Unit,
    onClearSnapshots: () -> Unit
) = Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    // tab row 显示快照日期
    if (snapshots.isNotEmpty()) {
        var selectedTabIndex by remember { mutableStateOf(0) }
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(
                    bottom = 16.dp,
                    top = if (currentPlatformInfo.isDesktop()) 48.dp else 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = onDismissRequest,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = TabRowDefaults.primaryContainerColor
                )
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.weight(1f).clip(CircleShape),
                edgePadding = 0.dp,
                indicator = {},
                divider = {}
            ) {
                snapshots.fastForEachIndexed { index, snapshot ->
                    val date = Instant.fromEpochMilliseconds(snapshot.createdAt)
                    val selected = selectedTabIndex == index
                    Tab(
                        text = {
                            Text(
                                text = date.toString(),
                                maxLines = 1,
                                style = if (selected) MaterialTheme.typography.titleMedium
                                else MaterialTheme.typography.titleSmall
                            )
                        },
                        selected = selected,
                        onClick = { selectedTabIndex = index },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            OutlinedIconButton(
                onClick = onClearSnapshots,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = TabRowDefaults.primaryContainerColor
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CleaningServices,
                    contentDescription = stringResource(Res.string.delete_all)
                )
            }
        }
        val backgroundColor = MaterialTheme.colorScheme.surface
        var diffResult by remember { mutableIntStateOf(0) }
        DiffText(
            text = currentContent,
            oldText = snapshots[selectedTabIndex].content,
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .weight(1f)
                .drawWithContent {
                    val brushTop = verticalGradient(
                        colors = listOf(backgroundColor, Color.Transparent),
                        startY = 0f,
                        endY = 8.dp.toPx()
                    )
                    val brushBottom = verticalGradient(
                        colors = listOf(Color.Transparent, backgroundColor),
                        startY = size.height - 8.dp.toPx(),
                        endY = size.height
                    )
                    drawRect(color = backgroundColor)
                    drawContent()
                    drawRect(brush = brushTop)
                    drawRect(brush = brushBottom)
                }
                .padding(horizontal = 8.dp),
            onDiffResult = { diffResult = it }
        )
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
                .background(backgroundColor, CircleShape)
                .padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Difference, contentDescription = "Difference")
            Text(": ${formatNumber(diffResult)}", modifier = Modifier.padding(end = 16.dp))
            TextButton(
                modifier = Modifier.padding(end = 4.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                onClick = {
                    onDeleteSnapshot(snapshots[selectedTabIndex])
                    selectedTabIndex = 0
                }
            ) {
                Text(stringResource(Res.string.delete))
            }
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                onClick = { onRestoreSnapshot(snapshots[selectedTabIndex]) }
            ) {
                Text(stringResource(Res.string.restore))
            }
        }
    } else {
        Text(
            text = stringResource(Res.string.no_snapshots),
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        LaunchedEffect(Unit) {
            delay(1000L)
            onDismissRequest()
        }
    }
}