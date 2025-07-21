package org.yangdai.kori.presentation.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.modify
import kori.composeapp.generated.resources.name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.defaultFolderColor
import org.yangdai.kori.data.local.entity.folderColorOptions
import org.yangdai.kori.presentation.component.HorizontalScrollbar

@OptIn(
    ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SharedTransitionScope.ModifyFolderDialog(
    folder: FolderEntity?,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    onConfirm: (FolderEntity) -> Unit
) = AnimatedContent(
    modifier = modifier,
    targetState = folder,
    transitionSpec = { fadeIn() togetherWith fadeOut() }
) { targetState ->
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (targetState != null) {

            Canvas(
                Modifier.fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { onDismissRequest() }) }
            ) {
                drawRect(Color.Black, alpha = 0.6f)
            }

            var isStarred by remember { mutableStateOf(targetState.isStarred) }
            val textFieldState = rememberTextFieldState(initialText = targetState.name)
            var color by remember { mutableLongStateOf(targetState.colorValue) }
            var isError by remember { mutableStateOf(false) }
            LaunchedEffect(textFieldState.text) {
                isError = false
            }
            val custom by remember {
                derivedStateOf {
                    !folderColorOptions.contains(Color(color))
                }
            }

            var showColorPicker by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val focusManager = LocalFocusManager.current
            val haptic = LocalHapticFeedback.current

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp)
                    .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "${targetState.id}-bounds"),
                        animatedVisibilityScope = this@AnimatedContent,
                        clipInOverlayDuringTransition = OverlayClip(dialogShape())
                    )
                    .background(
                        color = AlertDialogDefaults.containerColor,
                        shape = dialogShape()
                    )
                    .clip(dialogShape())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp, top = 16.dp)
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
                    FilledIconToggleButton(
                        checked = isStarred,
                        onCheckedChange = {
                            if (it)
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            else
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            isStarred = it
                        },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
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
                    onKeyboardAction = { focusManager.clearFocus() },
                    placeholder = { Text(stringResource(Res.string.name)) }
                )
                Box(
                    Modifier.padding(vertical = 8.dp).fillMaxWidth().background(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    val state = rememberLazyListState()
                    LazyRow(
                        state = state,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(folderColorOptions.size + 2) {
                            when (it) {
                                0 -> {
                                    OutlinedTextCircle(
                                        selected = color == defaultFolderColor,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            color = defaultFolderColor
                                        }
                                    )
                                }

                                folderColorOptions.size + 1 -> {
                                    OutlinedCustomCircle(
                                        color = if (custom) Color(color) else Color.Black,
                                        selected = custom,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            showColorPicker = true
                                        }
                                    )
                                }

                                else -> {
                                    OutlinedCircle(
                                        color = folderColorOptions[it - 1],
                                        selected = Color(color) == folderColorOptions[it - 1],
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            color = folderColorOptions[it - 1].toArgb().toLong()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalScrollbar(
                        state = state,
                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (targetState.id.isNotEmpty())
                        IconButton(
                            colors = IconButtonDefaults.outlinedIconButtonVibrantColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = IconButtonDefaults.smallSquareShape,
                            onClick = onDeleteRequest,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderDelete,
                                contentDescription = null
                            )
                        }
                    else Spacer(Modifier.size(48.dp))

                    Row {
                        DismissButton(onDismissRequest)
                        Spacer(Modifier.width(8.dp))
                        val haptic = LocalHapticFeedback.current
                        ConfirmButton {
                            if (textFieldState.text.isBlank()) {
                                isError = true
                                return@ConfirmButton
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
private fun OutlinedCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) = OutlinedIconButton(
    colors = IconButtonDefaults.outlinedIconButtonVibrantColors(containerColor = color),
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
    onClick = onClick
) { }

@Composable
private fun OutlinedCustomCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) = OutlinedIconButton(
    colors = IconButtonDefaults.outlinedIconButtonVibrantColors(containerColor = color),
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
    onClick = onClick
) { Icon(imageVector = Icons.Outlined.Colorize, contentDescription = null) }

@Composable
private fun OutlinedTextCircle(
    selected: Boolean,
    onClick: () -> Unit
) = OutlinedIconButton(
    colors = IconButtonDefaults.outlinedIconButtonVibrantColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
    onClick = onClick
) { Text("A") }