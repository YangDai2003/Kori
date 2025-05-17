package org.yangdai.kori.presentation.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import org.yangdai.kori.data.local.entity.defaultFolderColor
import org.yangdai.kori.data.local.entity.folderColorOptions
import org.yangdai.kori.presentation.component.HorizontalLazyListScrollbar

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ModifyFolderDialog(
    folder: FolderEntity?,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: (FolderEntity) -> Unit
) = AnimatedContent(
    modifier = modifier,
    targetState = folder,
    transitionSpec = {
        fadeIn() togetherWith fadeOut()
    }
) { targetState ->
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (targetState != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) { onDismissRequest() }
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            )

            var isStarred by remember { mutableStateOf(targetState.isStarred) }
            val textFieldState = rememberTextFieldState(initialText = targetState.name)
            var color by remember { mutableLongStateOf(targetState.colorValue) }
            var isError by remember { mutableStateOf(false) }
            LaunchedEffect(textFieldState.text) {
                isError = false
            }
            val custom = !folderColorOptions.contains(Color(color))

            var showColorPicker by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val bottomSheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)

            Column(
                modifier = Modifier
                    .imePadding()
                    .padding(16.dp)
                    .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "${targetState.id}-bounds"),
                        animatedVisibilityScope = this@AnimatedContent,
                        clipInOverlayDuringTransition = OverlayClip(AlertDialogDefaults.shape)
                    )
                    .background(
                        color = AlertDialogDefaults.containerColor,
                        shape = AlertDialogDefaults.shape
                    )
                    .clip(AlertDialogDefaults.shape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.modify),
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = AlertDialogDefaults.titleContentColor)
                    )
                    IconToggleButton(
                        checked = isStarred,
                        onCheckedChange = { isStarred = it },
                        colors = IconButtonDefaults.iconToggleButtonColors()
                            .copy(checkedContentColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(
                            imageVector = if (isStarred) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                            contentDescription = null
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = textFieldState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    isError = isError,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    placeholder = { Text(stringResource(Res.string.name)) }
                )
                Box {
                    val state = rememberLazyListState()
                    LazyRow(
                        state = state,
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(folderColorOptions.size + 2) {
                            when (it) {
                                0 -> {
                                    ColoredCircle2(selected = color == defaultFolderColor) {
                                        color = defaultFolderColor
                                    }
                                }

                                folderColorOptions.size + 1 -> {
                                    ColoredCircle3(
                                        background = if (custom) Color(color) else Color.Black,
                                        selected = custom
                                    ) {
                                        showColorPicker = true
                                    }
                                }

                                else -> {
                                    ColoredCircle(
                                        color = folderColorOptions[it - 1],
                                        selected = Color(color) == folderColorOptions[it - 1],
                                        onClick = {
                                            color = folderColorOptions[it - 1].toArgb().toLong()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalLazyListScrollbar(
                        state = state,
                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onDismissRequest) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    val haptic = LocalHapticFeedback.current
                    Button(
                        onClick = {
                            if (textFieldState.text.isBlank()) {
                                isError = true
                                return@Button
                            }

                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)

                            onConfirm(
                                FolderEntity(
                                    id = targetState.id,
                                    name = textFieldState.text.toString(),
                                    colorValue = color,
                                    isStarred = isStarred,
                                    createdAt = targetState.createdAt
                                )
                            )
                        }
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }

            if (showColorPicker) {
                ColorPickerBottomSheet(
                    oColor = if (custom) Color(color) else Color.White,
                    sheetState = bottomSheetState,
                    onDismissRequest = { showColorPicker = false }
                ) {
                    color = it.toLong()
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showColorPicker = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColoredCircle(
    background: Color = MaterialTheme.colorScheme.onSurface,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) = Box(
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

@Composable
private fun ColoredCircle2(
    background: Color = MaterialTheme.colorScheme.onSurface,
    selected: Boolean,
    onClick: () -> Unit
) = Box(
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
    Text("A")
}

@Composable
private fun ColoredCircle3(background: Color, selected: Boolean, onClick: () -> Unit) =
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
