package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.confirm
import kori.composeapp.generated.resources.modify
import kori.composeapp.generated.resources.name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.FolderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyFolderDialog(
    folder: FolderEntity,
    onDismissRequest: () -> Unit,
    onModify: (FolderEntity) -> Unit
) {
    var isStarred by remember { mutableStateOf(folder.isStarred) }
    var text by remember { mutableStateOf(folder.name) }
    var color by remember { mutableLongStateOf(folder.colorValue) }
    var isError by remember { mutableStateOf(false) }
    val custom = !FolderEntity.colors.contains(Color(color))
    val initValue =
        if (folder.colorValue == FolderEntity.defaultColorValue) 0
        else if (custom) FolderEntity.colors.size + 1
        else FolderEntity.colors.indexOf(Color(folder.colorValue)) + 1
    var selectedIndex by remember { mutableIntStateOf(initValue) }

    var showDialog by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(Res.string.modify))
                IconToggleButton(
                    checked = isStarred,
                    onCheckedChange = { isStarred = it },
                    colors = IconButtonDefaults.iconToggleButtonColors()
                        .copy(checkedContentColor = Color.Yellow)
                ) {
                    Icon(
                        imageVector = if (isStarred) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                        contentDescription = null
                    )
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = text,
                    onValueChange = {
                        text = it
                        isError = false // 当用户输入时重置错误状态
                    },
                    singleLine = true,
                    isError = isError,
                    placeholder = { Text(text = stringResource(Res.string.name)) },
                )
                LazyRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(FolderEntity.colors.size + 2) {
                        when (it) {
                            0 -> {
                                ColoredCircle2(selected = 0 == selectedIndex) {
                                    selectedIndex = 0
                                }
                            }

                            FolderEntity.colors.size + 1 -> {
                                ColoredCircle3(
                                    background = if (custom) Color(color) else Color.Black,
                                    selected = FolderEntity.colors.size + 1 == selectedIndex
                                ) {
                                    selectedIndex = FolderEntity.colors.size + 1
                                    showDialog = true
                                }
                            }

                            else -> {
                                ColoredCircle(
                                    color = FolderEntity.colors[it - 1],
                                    selected = it == selectedIndex,
                                    onClick = { selectedIndex = it }
                                )
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(
                onClick = {
                    if (text.isBlank()) {
                        isError = true
                        return@Button
                    }

                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)

                    color = when (selectedIndex) {
                        0 -> FolderEntity.defaultColorValue
                        FolderEntity.colors.size + 1 -> color
                        else -> FolderEntity.colors[selectedIndex - 1].toArgb().toLong()
                    }

                    onModify(
                        FolderEntity(
                            id = folder.id,
                            name = text,
                            colorValue = color,
                            isStarred = isStarred,
                            createdAt = folder.createdAt
                        )
                    )

                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showDialog) {
        ColorPickerBottomSheet(
            initialColor = if (custom) Color(color) else Color.White,
            sheetState = bottomSheetState,
            onDismissRequest = { showDialog = false }
        ) {
            color = it.toLong()
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    showDialog = false
                }
            }
        }
    }
}

@Composable
fun ColoredCircle(color: Color, selected: Boolean, onClick: () -> Unit) {

    val background = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(shape = CircleShape)
                .background(color = color)
        )
    }
}

@Composable
fun ColoredCircle2(selected: Boolean, onClick: () -> Unit) {

    val background = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "A")
    }
}

@Composable
fun ColoredCircle3(background: Color, selected: Boolean, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Outlined.Colorize, contentDescription = null)
    }
}
