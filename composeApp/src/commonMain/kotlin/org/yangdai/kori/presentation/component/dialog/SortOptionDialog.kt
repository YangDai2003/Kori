package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSortOptionDialog(
    initialNoteSortType: NoteSortType = NoteSortType.NAME_ASC,
    onDismissRequest: () -> Unit,
    onSortTypeSelected: (NoteSortType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetMaxWidth = 480.dp
    ) {
        Text(
            text = stringResource(Res.string.sort_by),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
        )

        Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
            NoteSortType.entries.forEach { sortType ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (sortType == initialNoteSortType),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                if (sortType != initialNoteSortType) {
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
                            selected = (sortType == initialNoteSortType),
                            onClick = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSortOptionDialog(
    initialFolderSortType: FolderSortType = FolderSortType.NAME_ASC,
    onDismissRequest: () -> Unit,
    onSortTypeSelected: (FolderSortType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetMaxWidth = 480.dp
    ) {
        Text(
            text = stringResource(Res.string.sort_by),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
        )

        Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
            FolderSortType.entries.forEach { sortType ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (sortType == initialFolderSortType),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                if (sortType != initialFolderSortType) {
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
                            selected = (sortType == initialFolderSortType),
                            onClick = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
