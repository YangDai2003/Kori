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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    transitionSpec = {
        fadeIn() togetherWith fadeOut()
    }
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
            val bottomSheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val focusManager = LocalFocusManager.current

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
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
                        onCheckedChange = { isStarred = it },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.tertiary
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onKeyboardAction = { focusManager.clearFocus() },
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
                                    OutlinedTextCircle(
                                        selected = color == defaultFolderColor,
                                        onClick = { color = defaultFolderColor }
                                    )
                                }

                                folderColorOptions.size + 1 -> {
                                    OutlinedCustomCircle(
                                        color = if (custom) Color(color) else Color.Black,
                                        selected = custom,
                                        onClick = { showColorPicker = true }
                                    )
                                }

                                else -> {
                                    OutlinedCircle(
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (targetState.id.isNotEmpty())
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.outlinedIconButtonVibrantColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            onClick = onDeleteRequest,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderDelete,
                                contentDescription = null
                            )
                        }
                    else Spacer(Modifier.size(48.dp))

                    Row {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OutlinedCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) = OutlinedIconButton(
    shapes = IconButtonDefaults.shapes(),
    colors = IconButtonDefaults.outlinedIconButtonVibrantColors(containerColor = color),
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
    onClick = onClick
) { }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OutlinedCustomCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) = OutlinedIconButton(
    shapes = IconButtonDefaults.shapes(),
    colors = IconButtonDefaults.outlinedIconButtonVibrantColors(containerColor = color),
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
    onClick = onClick
) { Icon(imageVector = Icons.Outlined.Colorize, contentDescription = null) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OutlinedTextCircle(
    selected: Boolean,
    onClick: () -> Unit
) = OutlinedIconButton(
    shapes = IconButtonDefaults.shapes(),
    colors = IconButtonDefaults.outlinedIconButtonVibrantColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
    onClick = onClick
) { Text("A") }