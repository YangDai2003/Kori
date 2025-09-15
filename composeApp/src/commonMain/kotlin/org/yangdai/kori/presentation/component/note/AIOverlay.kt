package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.describe_the_note_you_want_to_generate
import kori.composeapp.generated.resources.elaborate
import kori.composeapp.generated.resources.proofread
import kori.composeapp.generated.resources.rewrite
import kori.composeapp.generated.resources.shorten
import kori.composeapp.generated.resources.summarize
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.login.brandColor1
import org.yangdai.kori.presentation.component.login.brandColor2
import org.yangdai.kori.presentation.component.login.brandColor3

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun LoadingScrim() {
    BackHandler {}
    Box(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .pointerInput(Unit) { }
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        ContainedLoadingIndicator()
    }
}

@Composable
private fun AIAssistChip(
    onClick: () -> Unit,
    label: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) .85f else 1f,
        animationSpec = if (isPressed)
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        else
            spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioHighBouncy,
            ),
        visibilityThreshold = .000001f
    )
    AssistChip(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .graphicsLayer {
                scaleY = scale
                scaleX = (1f - scale) + 1f
            }
            .focusProperties { canFocus = false },
        interactionSource = interactionSource,
        onClick = onClick,
        label = { Text(label) },
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(brandColor1, brandColor2, brandColor3)
            )
        ),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.8f)
        )
    )
}

sealed interface AIAssistEvent {
    data object Rewrite : AIAssistEvent
    data object Summarize : AIAssistEvent
    data object Proofread : AIAssistEvent
    data object Shorten : AIAssistEvent
    data object Elaborate : AIAssistEvent
    data class Generate(val prompt: String) : AIAssistEvent
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AIAssist(
    isGenerating: Boolean,
    isTextSelectionCollapsed: Boolean,
    onEvent: (AIAssistEvent) -> Unit
) = BoxWithConstraints(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomStart
) {
    val prompt = rememberTextFieldState()
    var inputMode by rememberSaveable { mutableStateOf(false) }
    var showChips by rememberSaveable { mutableStateOf(true) }
    if (!isTextSelectionCollapsed && showChips) {
        Row(
            modifier = Modifier.imePadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
                .padding(bottom = 48.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(Modifier.width(6.dp))
            AIAssistChip(
                onClick = { onEvent(AIAssistEvent.Proofread) },
                label = stringResource(Res.string.proofread)
            )
            AIAssistChip(
                onClick = { onEvent(AIAssistEvent.Shorten) },
                label = stringResource(Res.string.shorten)
            )
            AIAssistChip(
                onClick = { onEvent(AIAssistEvent.Elaborate) },
                label = stringResource(Res.string.elaborate)
            )
            AIAssistChip(
                onClick = { onEvent(AIAssistEvent.Rewrite) },
                label = stringResource(Res.string.rewrite)
            )
            AIAssistChip(
                onClick = { onEvent(AIAssistEvent.Summarize) },
                label = stringResource(Res.string.summarize)
            )
            Spacer(Modifier.width(6.dp))
        }
    }

    val backgroundColor by animateColorAsState(
        if (inputMode) MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        else Color.Transparent
    )
    if (inputMode) {
        Canvas(
            Modifier.fillMaxSize().pointerInput(isGenerating) {
                detectTapGestures {
                    if (isGenerating) return@detectTapGestures
                    inputMode = false
                }
            }
        ) {
            drawRect(backgroundColor)
        }
        BackHandler { inputMode = false }
    }

    val widthModifier =
        if (maxWidth >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND.dp)
            Modifier.fillMaxWidth(0.25f)
        else if (maxWidth >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND.dp)
            Modifier.fillMaxWidth(0.33f)
        else if (maxWidth >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND.dp)
            Modifier.fillMaxWidth(0.5f)
        else Modifier.fillMaxWidth()
    val verticalPadding by animateDpAsState(if (inputMode) 16.dp else 4.dp)
    val horizontalPadding by animateDpAsState(if (inputMode) 16.dp else 8.dp)
    val shadowRadius by animateFloatAsState(if (inputMode) 60f else 0f)
    Box(
        modifier = Modifier.imePadding()
            .navigationBarsPadding()
            .displayCutoutPadding()
            .padding(vertical = verticalPadding, horizontal = horizontalPadding)
            .dropShadow(MaterialTheme.shapes.extraLargeIncreased) {
                radius = shadowRadius
                brush = Brush.verticalGradient(
                    colors = listOf(brandColor1, brandColor2, brandColor3)
                )
            }
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.extraLargeIncreased,
                brush = Brush.verticalGradient(
                    colors = listOf(brandColor1, brandColor2, brandColor3)
                ),
            )
            .wrapContentSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.extraLargeIncreased,
            )
            .innerShadow(MaterialTheme.shapes.extraLargeIncreased) {
                radius = 90f
                brush = Brush.verticalGradient(
                    colors = listOf(brandColor1, brandColor2, brandColor3)
                )
                alpha = .4f
            },
        contentAlignment = Alignment.BottomStart
    ) {
        AnimatedVisibility(
            visible = inputMode,
            enter = fadeIn() + expandIn(expandFrom = Alignment.BottomStart),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomStart)
        ) {
            val focusRequester = remember { FocusRequester() }
            OutlinedTextField(
                modifier = widthModifier.focusRequester(focusRequester).onPreviewKeyEvent {
                    if (it.isCtrlPressed && it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                        if (inputMode && !isGenerating) {
                            onEvent(AIAssistEvent.Generate(prompt.text.toString()))
                            true
                        } else {
                            false
                        }
                    } else false
                },
                state = prompt,
                readOnly = isGenerating,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = MaterialTheme.shapes.extraLargeIncreased,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                ),
                trailingIcon = { Spacer(Modifier.size(48.dp)) },
                placeholder = {
                    Text(
                        stringResource(Res.string.describe_the_note_you_want_to_generate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
            LaunchedEffect(Unit) {
                delay(300L)
                focusRequester.requestFocus()
            }
        }

        val padding by animateDpAsState(if (inputMode) 8.dp else 0.dp)
        Crossfade(
            isGenerating,
            modifier = Modifier.padding(bottom = padding, end = padding)
                .align(Alignment.BottomEnd)
        ) {
            if (it)
                LoadingIndicator(
                    Modifier
                        .padding(4.dp)
                        .size(
                            IconButtonDefaults.extraSmallContainerSize(
                                widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform
                            )
                        )
                )
            else
                FilledIconButton(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .focusProperties { canFocus = false }
                        .padding(4.dp)
                        .size(
                            IconButtonDefaults.extraSmallContainerSize(
                                widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform
                            )
                        ),
                    onClick = {
                        if (isTextSelectionCollapsed) {
                            if (inputMode) {
                                onEvent(AIAssistEvent.Generate(prompt.text.toString()))
                            } else {
                                inputMode = true
                            }
                        } else {
                            showChips = !showChips
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                        imageVector = Icons.Outlined.GeneratingTokens,
                        contentDescription = null
                    )
                }
        }
    }

    AnimatedVisibility(
        visible = isGenerating && !isTextSelectionCollapsed,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LoadingScrim()
    }
}