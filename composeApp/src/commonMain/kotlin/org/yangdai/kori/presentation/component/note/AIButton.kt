package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.yangdai.kori.presentation.component.login.brandColor1
import org.yangdai.kori.presentation.component.login.brandColor2
import org.yangdai.kori.presentation.component.login.brandColor3

data class GenerateNoteButtonState(
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun GenerateNoteButton(
    state: GenerateNoteButtonState,
    startGenerating: (prompt: String) -> Unit
) {
    val prompt = rememberTextFieldState()
    var inputMode by remember { mutableStateOf(false) }
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
                Modifier.fillMaxSize().pointerInput(state.isGenerating) {
                    detectTapGestures {
                        if (state.isGenerating) return@detectTapGestures
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
        val verticalPadding by animateDpAsState(if (inputMode) 16.dp else 56.dp)
        val horizontalPadding by animateDpAsState(if (inputMode) 16.dp else 8.dp)
        val shadowRadius by animateFloatAsState(if (inputMode) 60f else 0f)
        Box(
            modifier = Modifier.imePadding()
                .systemBarsPadding()
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
                enter = slideInHorizontally { -it } + expandIn(expandFrom = Alignment.BottomStart),
                exit = slideOutHorizontally { -it } + shrinkOut(shrinkTowards = Alignment.BottomStart)
            ) {
                val focusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    modifier = widthModifier.focusRequester(focusRequester),
                    state = prompt,
                    readOnly = state.isGenerating,
                    isError = state.errorMessage != null,
                    shape = MaterialTheme.shapes.extraLargeIncreased,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                    ),
                    trailingIcon = { Spacer(Modifier.size(48.dp)) },
                    placeholder = { Text("描述你想创建的笔记内容") }
                )
                LaunchedEffect(Unit) {
                    delay(300L)
                    focusRequester.requestFocus()
                }
            }

            val padding by animateDpAsState(if (inputMode) 4.dp else 0.dp)
            Crossfade(
                state.isGenerating,
                modifier = Modifier.padding(bottom = padding, end = padding)
                    .align(Alignment.BottomEnd)
            ) { isGenerating ->
                if (isGenerating)
                    LoadingIndicator()
                else
                    FilledIconButton(
                        onClick = {
                            if (inputMode) startGenerating(prompt.text.toString())
                            else inputMode = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GeneratingTokens,
                            contentDescription = null
                        )
                    }
            }
        }
    }
}

@Preview
@Composable
fun GenerateNoteButtonPreview() {
    var state by remember { mutableStateOf(GenerateNoteButtonState()) }
    GenerateNoteButton(
        state = state,
        startGenerating = { state = state.copy(isGenerating = true) }
    )
}