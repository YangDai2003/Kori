package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.sort_by
import kori.composeapp.generated.resources.sort_created_a
import kori.composeapp.generated.resources.sort_created_d
import kori.composeapp.generated.resources.sort_modified_a
import kori.composeapp.generated.resources.sort_modified_d
import kori.composeapp.generated.resources.sort_name_a
import kori.composeapp.generated.resources.sort_name_d
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.domain.sort.FolderSortType
import org.yangdai.kori.domain.sort.NoteSortType

@Composable
private fun DragHandle() = Surface(
    modifier = Modifier.padding(vertical = 12.dp).semantics {
        contentDescription = "Drag handle for sorting options"
    },
    color = MaterialTheme.colorScheme.outlineVariant,
    shape = MaterialTheme.shapes.extraLarge,
) {
    Box(Modifier.size(width = 32.dp, height = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSortOptionBottomSheet(
    oNoteSortType: NoteSortType = NoteSortType.NAME_ASC,
    onDismissRequest: () -> Unit,
    onSortTypeSelected: (NoteSortType) -> Unit
) = ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle = { DragHandle() },
    sheetMaxWidth = 480.dp
) {
    val haptic = LocalHapticFeedback.current
    Text(
        text = stringResource(Res.string.sort_by),
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )
    Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
        NoteSortType.entries.forEach { sortType ->
            ListItem(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .selectable(
                        selected = (sortType == oNoteSortType),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            if (sortType != oNoteSortType) {
                                onSortTypeSelected(sortType)
                            }
                            onDismissRequest()
                        },
                        role = Role.RadioButton
                    ),
                headlineContent = {
                    val text = when (sortType) {
                        NoteSortType.NAME_ASC -> stringResource(Res.string.sort_name_a)
                        NoteSortType.NAME_DESC -> stringResource(Res.string.sort_name_d)
                        NoteSortType.CREATE_TIME_ASC -> stringResource(Res.string.sort_created_a)
                        NoteSortType.CREATE_TIME_DESC -> stringResource(Res.string.sort_created_d)
                        NoteSortType.UPDATE_TIME_ASC -> stringResource(Res.string.sort_modified_a)
                        NoteSortType.UPDATE_TIME_DESC -> stringResource(Res.string.sort_modified_d)
                    }
                    Text(text)
                },
                trailingContent = {
                    RadioButton(
                        selected = (sortType == oNoteSortType),
                        onClick = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSortOptionBottomSheet(
    oFolderSortType: FolderSortType = FolderSortType.NAME_ASC,
    onDismissRequest: () -> Unit,
    onSortTypeSelected: (FolderSortType) -> Unit
) = ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle = { DragHandle() },
    sheetMaxWidth = 480.dp
) {
    val haptic = LocalHapticFeedback.current
    Text(
        text = stringResource(Res.string.sort_by),
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )
    Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
        FolderSortType.entries.forEach { sortType ->
            ListItem(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .selectable(
                        selected = (sortType == oFolderSortType),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            if (sortType != oFolderSortType) {
                                onSortTypeSelected(sortType)
                            }
                            onDismissRequest()
                        },
                        role = Role.RadioButton
                    ),
                headlineContent = {
                    val text = when (sortType) {
                        FolderSortType.NAME_ASC -> stringResource(Res.string.sort_name_a)
                        FolderSortType.NAME_DESC -> stringResource(Res.string.sort_name_d)
                        FolderSortType.CREATE_TIME_ASC -> stringResource(Res.string.sort_created_a)
                        FolderSortType.CREATE_TIME_DESC -> stringResource(Res.string.sort_created_d)
                    }
                    Text(text)
                },
                trailingContent = {
                    RadioButton(
                        selected = (sortType == oFolderSortType),
                        onClick = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
