package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.VerticalScrollbar
import org.yangdai.kori.presentation.component.note.markdown.MarkdownLint
import org.yangdai.kori.presentation.util.isScreenSizeLarge
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun TextEditor(
    modifier: Modifier,
    textFieldModifier: Modifier,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit,
    headerRange: IntRange? = null,
    isLintActive: Boolean = false,
    outputTransformation: OutputTransformation? = null
) {
    var matchedWordsRanges by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var currentRangeIndex by remember { mutableIntStateOf(0) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Search functionality
    LaunchedEffect(state.text, findAndReplaceState.searchWord, readMode) {
        matchedWordsRanges = if (!readMode && findAndReplaceState.searchWord.isNotBlank()) {
            findAllRanges(state.text.toString(), findAndReplaceState.searchWord)
        } else {
            emptyList()
        }
    }

    // Handle search navigation
    LaunchedEffect(matchedWordsRanges, findAndReplaceState.scrollDirection) {
        onFindAndReplaceUpdate(
            findAndReplaceState.copy(
                position = if (matchedWordsRanges.isNotEmpty()) "${currentRangeIndex + 1}/${matchedWordsRanges.size}" else ""
            )
        )
        if (matchedWordsRanges.isNotEmpty() && textLayoutResult != null && findAndReplaceState.scrollDirection != null) {
            // Update current index
            currentRangeIndex = when (findAndReplaceState.scrollDirection) {
                ScrollDirection.NEXT -> (currentRangeIndex + 1).takeIf { it < matchedWordsRanges.size }
                    ?: 0 // Wrap around
                ScrollDirection.PREVIOUS -> (currentRangeIndex - 1).takeIf { it >= 0 }
                    ?: (matchedWordsRanges.size - 1) // Wrap around
            }

            // Get the target match position
            val targetMatch = matchedWordsRanges[currentRangeIndex]
            // Get the text bounds
            val bounds = textLayoutResult!!.getBoundingBox(targetMatch.first)
            // Calculate scroll position
            val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
            // Execute scroll
            scrollState.animateScrollTo(scrollPosition)
            // Notify scroll completion
            onFindAndReplaceUpdate(findAndReplaceState.copy(scrollDirection = null))
        }
    }

    // Handle header navigation
    LaunchedEffect(headerRange) {
        headerRange?.let {
            textLayoutResult?.let { layout ->
                // Get bounds of that position
                val bounds = layout.getBoundingBox(headerRange.first)
                // Calculate scroll position
                val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
                // Execute scroll
                scrollState.animateScrollTo(scrollPosition)
            }
        }
    }

    // Handle replace functionality
    LaunchedEffect(findAndReplaceState.replaceType) {
        if (findAndReplaceState.replaceType != null && findAndReplaceState.searchWord.isNotBlank()) {
            if (findAndReplaceState.replaceType == ReplaceType.ALL) {
                val currentText = state.text.toString()
                val newText = currentText.replace(
                    findAndReplaceState.searchWord,
                    findAndReplaceState.replaceWord
                )
                state.setTextAndPlaceCursorAtEnd(newText)
            } else if (findAndReplaceState.replaceType == ReplaceType.CURRENT) {
                // Check if index is valid
                if (matchedWordsRanges.isEmpty() || currentRangeIndex >= matchedWordsRanges.size) return@LaunchedEffect
                // Get the position to be replaced
                val (startIndex, endIndex) = matchedWordsRanges[currentRangeIndex]
                // Execute replacement
                state.edit {
                    replace(startIndex, endIndex, findAndReplaceState.replaceWord)
                }
            }
            onFindAndReplaceUpdate(findAndReplaceState.copy(replaceType = null))
        }
    }

    val actualLinePositions by remember(showLineNumbers, textLayoutResult) {
        derivedStateOf {
            if (showLineNumbers && textLayoutResult != null) {
                // Calculate line positions only if line numbers are needed and layout result is available
                val currentText = state.text
                val lineCount = textLayoutResult!!.lineCount
                buildList {
                    for (lineIndex in 0 until lineCount) {
                        // Get the starting character offset of the line
                        val lineStartOffset = textLayoutResult!!.getLineStart(lineIndex)
                        val isNewLine = lineIndex == 0 ||
                                (lineStartOffset > 0 && lineStartOffset <= currentText.length && currentText[lineStartOffset - 1] == '\n')
                        if (isNewLine) {
                            val lineTopY = textLayoutResult!!.getLineTop(lineIndex)
                            add(lineStartOffset to lineTopY)
                        }
                    }
                }
            } else {
                emptyList()
            }
        }
    }

    val currentLine by remember(state.selection) {
        derivedStateOf {
            if (actualLinePositions.isEmpty()) 0
            else {
                val selectionStart = state.selection.start
                // 使用二分查找找到最后一个小于等于 selectionStart 的位置
                var left = 0
                var right = actualLinePositions.size - 1
                var result = 0

                while (left <= right) {
                    val mid = left + (right - left) / 2
                    if (actualLinePositions[mid].first <= selectionStart) {
                        result = mid
                        left = mid + 1
                    } else {
                        right = mid - 1
                    }
                }
                result
            }
        }
    }

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
    var lintJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(state.text, isLintActive) {
        lintJob?.cancel()
        if (isLintActive) {
            lintJob = coroutineScope.launch {
                delay(300L) // 300ms 防抖
                withContext(Dispatchers.Default) {
                    lintErrors = MarkdownLint().validate(state.text.toString())
                }
            }
        } else {
            lintErrors = emptyList()
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
        Box(Modifier.fillMaxSize()) {
            BasicTextField(
                modifier = textFieldModifier,
                scrollState = scrollState,
                readOnly = readMode,
                state = state,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { textLayoutResult = it() },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                outputTransformation = outputTransformation,
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .clipToBounds()
                            .drawWithCache {
                                val searchPaths = if (findAndReplaceState.searchWord.isNotBlank()) {
                                    matchedWordsRanges.mapNotNull { (start, end) ->
                                        // 安全检查
                                        if (start < end && end <= state.text.length && start >= 0) {
                                            textLayoutResult?.getPathForRange(start, end)
                                        } else {
                                            null
                                        }
                                    }
                                } else emptyList()

                                val lintPaths = if (isLintActive) {
                                    lintErrors.mapNotNull { issue ->
                                        if (issue.startIndex < issue.endIndex && issue.endIndex <= state.text.length) {
                                            textLayoutResult?.getPathForRange(
                                                issue.startIndex,
                                                issue.endIndex
                                            )
                                        } else {
                                            null
                                        }
                                    }
                                } else emptyList()

                                // 预先计算颜色，避免在绘制循环中重复创建
                                val highlightBgColor = Color.Cyan.copy(alpha = 0.5f)
                                val currentHighlightBorderColor = Color.Blue
                                val otherHighlightBorderColor = Color.Cyan

                                onDrawBehind {

                                    val currentPhase = phase

                                    textLayoutResult?.let {
                                        val scrollOffset = Offset(0f, -scrollState.value.toFloat())
                                        withTransform({
                                            translate(scrollOffset.x, scrollOffset.y)
                                        }) {
                                            // 绘制搜索高亮
                                            searchPaths.forEachIndexed { index, path ->
                                                drawPath(
                                                    path,
                                                    color = highlightBgColor,
                                                    alpha = 0.5f
                                                )
                                                val borderColor =
                                                    if (index == currentRangeIndex) currentHighlightBorderColor
                                                    else otherHighlightBorderColor
                                                drawPath(
                                                    path,
                                                    color = borderColor,
                                                    style = Stroke(width = 1.5f)
                                                )
                                            }

                                            // 绘制波浪线
                                            lintPaths.forEach {
                                                drawWavyUnderlineOptimized(it, currentPhase)
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
            if (!isScreenSizeLarge()) {
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    state = scrollState
                )
            }
        }
    }
}

private suspend fun findAllRanges(text: String, word: String): List<Pair<Int, Int>> {
    if (word.isBlank()) return emptyList()

    return withContext(Dispatchers.Default) {
        buildList {
            var index = text.indexOf(word)
            while (index != -1) {
                add(index to (index + word.length))
                index = text.indexOf(word, index + 1)
            }
        }
    }
}

private fun DrawScope.drawWavyUnderlineOptimized(
    path: Path,
    phase: Float,
    amplitude: Float = 3f,
    wavelength: Float = 25f,
    sampleStep: Float = (density * 2).coerceAtLeast(1f) // 根据屏幕密度自适应
) {
    val bounds = path.getBounds()
    val width = bounds.right - bounds.left
    if (width <= 0f) return

    val pointCount = (width / sampleStep).toInt().coerceAtLeast(2)
    val points = Array(pointCount) { i ->
        val x = bounds.left + i * sampleStep
        val y = bounds.bottom + 2f + amplitude * sin((x * (2f * PI / wavelength)) + phase).toFloat()
        Offset(x, y)
    }

    drawPoints(
        points = points.toList(),
        pointMode = PointMode.Polygon,
        color = Color.Red,
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )
}