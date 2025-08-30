package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GeneratingTokens
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.describe_the_note_you_want_to_generate
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.login.brandColor1
import org.yangdai.kori.presentation.component.login.brandColor2
import org.yangdai.kori.presentation.component.login.brandColor3

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun GenerateNoteButton(startGenerating: (prompt: String, onSuccess: () -> Unit, onError: (errorMsg: String) -> Unit) -> Unit) {
    val prompt = rememberTextFieldState()
    var inputMode by rememberSaveable { mutableStateOf(false) }
    var isGenerating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
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
            if (maxWidth >= 1600.dp) Modifier.fillMaxWidth(0.25f)
            else if (maxWidth >= 1200.dp) Modifier.fillMaxWidth(0.33f)
            else if (maxWidth >= 800.dp) Modifier.fillMaxWidth(0.5f)
            else Modifier.fillMaxWidth()
        val verticalPadding by animateDpAsState(if (inputMode) 16.dp else 4.dp)
        val horizontalPadding by animateDpAsState(if (inputMode) 16.dp else 8.dp)
        val shadowRadius by animateFloatAsState(if (inputMode) 60f else 0f)
        Box(
            modifier = Modifier.imePadding()
                .systemBarsPadding()
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
                                isGenerating = true
                                startGenerating(
                                    prompt.text.toString(),
                                    {
                                        isGenerating = false
                                        inputMode = false
                                        prompt.clearText()
                                    },
                                    { errorMsg ->
                                        isGenerating = false
                                        errorMessage = errorMsg
                                    }
                                )
                                true
                            } else {
                                false
                            }
                        } else false
                    },
                    state = prompt,
                    readOnly = isGenerating,
                    isError = errorMessage != null,
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
                            .padding(4.dp)
                            .size(
                                IconButtonDefaults.extraSmallContainerSize(
                                    widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform
                                )
                            ),
                        onClick = {
                            if (inputMode) {
                                isGenerating = true
                                startGenerating(
                                    prompt.text.toString(),
                                    {
                                        isGenerating = false
                                        inputMode = false
                                        prompt.clearText()
                                    },
                                    { errorMsg ->
                                        isGenerating = false
                                        errorMessage = errorMsg
                                    }
                                )
                            } else inputMode = true
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
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LoadingScrim() {
    BackHandler {}
    Box(
        modifier = Modifier.fillMaxSize()
            .pointerInput(Unit) { }
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        ContainedLoadingIndicator()
    }
}