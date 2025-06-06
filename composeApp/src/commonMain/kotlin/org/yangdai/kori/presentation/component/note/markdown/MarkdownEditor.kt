package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.LineNumbersColumn
import org.yangdai.kori.presentation.component.note.TextEditorBase
import org.yangdai.kori.presentation.component.note.dragAndDropText
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MarkdownEditor(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit
) {

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "wavy-line")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-phase"
    )
    var lintErrors by remember { mutableStateOf(emptyList<MarkdownLint.Issue>()) }

    LaunchedEffect(state.text, isLintActive) {
        withContext(Dispatchers.Default) {
            lintErrors =
                if (isLintActive) MarkdownLint().validate(state.text.toString())
                else emptyList()
        }
    }

    TextEditorBase(
        state = state,
        scrollState = scrollState,
        readMode = readMode,
        showLineNumbers = showLineNumbers,
        headerRange = headerRange,
        findAndReplaceState = findAndReplaceState,
        onFindAndReplaceUpdate = onFindAndReplaceUpdate,
        textLayoutResult = textLayoutResult
    ) { indices, currentIndex, actualLinePositions ->
        val currentLine by remember(state.selection, actualLinePositions) {
            derivedStateOf {
                actualLinePositions.indexOfLast { (startIndex, _) ->
                    startIndex <= state.selection.start
                }.coerceAtLeast(0)
            }
        }
        Row(modifier) {
            if (showLineNumbers) {
                LineNumbersColumn(
                    currentLine = currentLine,
                    actualLinePositions = actualLinePositions,
                    scrollProvider = { scrollState.value }
                )
                VerticalDivider()
            }

            BasicTextField(
                // The contentReceiver modifier is used to receive text content from the clipboard or drag-and-drop operations.
                modifier = Modifier
                    .padding(start = if (showLineNumbers) 4.dp else 16.dp, end = 16.dp)
                    .fillMaxSize()
                    .markdownKeyEvents(state)
                    .dragAndDropText(state),
                scrollState = scrollState,
                readOnly = readMode,
                state = state,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { result -> textLayoutResult = result.invoke() },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .clipToBounds()
                            .drawBehind {
                                textLayoutResult?.let { layoutResult ->
                                    // 提前计算滚动偏移,避免重复计算
                                    val scrollOffset = Offset(0f, -scrollState.value.toFloat())
                                    val text = state.text.toString()

                                    // 批量绘制搜索高亮
                                    if (findAndReplaceState.searchWord.isNotBlank()) {
                                        // 使用 withTransform 避免多次 translate
                                        withTransform({
                                            translate(
                                                scrollOffset.x,
                                                scrollOffset.y
                                            )
                                        }) {
                                            indices.forEachIndexed { index, (start, end) ->
                                                if (start < end && end <= text.length) {
                                                    val path =
                                                        layoutResult.getPathForRange(start, end)
                                                    drawPath(
                                                        path = path,
                                                        color = if (index == currentIndex) Color.Green else Color.Cyan,
                                                        alpha = 0.5f,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 批量绘制波浪线
                                    if (isLintActive) {
                                        withTransform({
                                            translate(
                                                scrollOffset.x,
                                                scrollOffset.y
                                            )
                                        }) {
                                            lintErrors.forEach { issue ->
                                                val start = issue.startIndex
                                                val end = issue.endIndex
                                                if (start < end && end <= text.length) {
                                                    val path =
                                                        layoutResult.getPathForRange(start, end)
                                                    drawWavyUnderline(this, path, phase)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.content),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.6f
                                    )
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

private fun drawWavyUnderline(
    drawScope: DrawScope,
    path: Path,
    phase: Float,
    amplitude: Float = 3f,
    wavelength: Float = 25f
) {
    val bounds = path.getBounds()

    // 预计算正弦波点数
    val pointCount = ((bounds.right - bounds.left) * 2).toInt()
    if (pointCount <= 0) return

    val points = FloatArray(pointCount * 2)
    val startX = bounds.left
    val y = bounds.bottom + 2f

    // 批量计算波浪点
    for (i in 0 until pointCount) {
        val x = startX + i * 0.5f
        val yOffset = amplitude * sin((x * (2f * PI / wavelength)) + phase).toFloat()
        points[i * 2] = x
        points[i * 2 + 1] = y + yOffset
    }

    // 单次绘制所有点
    drawScope.drawPoints(
        points = points.toList().chunked(2).map { (x, y) -> Offset(x, y) },
        pointMode = PointMode.Polygon,
        color = Color.Red,
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )
}
