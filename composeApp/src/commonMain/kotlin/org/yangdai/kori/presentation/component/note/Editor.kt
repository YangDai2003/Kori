package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.VerticalScrollbar
import org.yangdai.kori.presentation.util.rememberIsScreenWidthExpanded
import kotlin.math.PI
import kotlin.math.sin

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun Editor(
    modifier: Modifier,
    textFieldModifier: Modifier,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    findAndReplaceState: FindAndReplaceState,
    readOnly: Boolean,
    isLineNumberVisible: Boolean,
    lint: Lint? = null,
    headerRange: IntRange? = null,
    outputTransformation: OutputTransformation? = null,
    onScroll: (firstVisibleCharPositon: Int) -> Unit = {}
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var matchedWordsRanges by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var currentRangeIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(textFieldState.text, findAndReplaceState.searchWord, readOnly) {
        val newRanges = if (readOnly) emptyList()
        else findAllRanges(textFieldState.text.toString(), findAndReplaceState.searchWord)
        if (newRanges != matchedWordsRanges) matchedWordsRanges = newRanges
    }

    // 处理用户导航（上一个/下一个）和滚动逻辑
    LaunchedEffect(findAndReplaceState.scrollDirection) {
        if (findAndReplaceState.scrollDirection != ScrollDirection.NONE && matchedWordsRanges.isNotEmpty()) {
            val newIndex = when (findAndReplaceState.scrollDirection) {
                ScrollDirection.NEXT ->
                    (currentRangeIndex + 1).takeIf { it < matchedWordsRanges.size } ?: 0

                ScrollDirection.PREVIOUS ->
                    (currentRangeIndex - 1).takeIf { it >= 0 } ?: (matchedWordsRanges.size - 1)

                else -> currentRangeIndex
            }
            if (newIndex != currentRangeIndex) currentRangeIndex = newIndex
            findAndReplaceState.scrollDirection = ScrollDirection.NONE
        }
    }

    // 响应索引和列表的变化，来更新UI和执行滚动
    LaunchedEffect(matchedWordsRanges, currentRangeIndex) {
        findAndReplaceState.position =
            if (matchedWordsRanges.isNotEmpty()) "${currentRangeIndex + 1}/${matchedWordsRanges.size}" else ""
        val layoutResult = textLayoutResult ?: return@LaunchedEffect
        if (matchedWordsRanges.isNotEmpty() && currentRangeIndex in matchedWordsRanges.indices) {
            val targetMatch = matchedWordsRanges[currentRangeIndex]
            val bounds = layoutResult.getBoundingBox(targetMatch.first)
            val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
            scrollState.animateScrollTo(scrollPosition)
        }
    }

    // 处理标题导航
    LaunchedEffect(headerRange) {
        headerRange?.let { range ->
            textLayoutResult?.let { layout ->
                val bounds = layout.getBoundingBox(range.first)
                val scrollPosition = bounds.top.toInt().coerceAtLeast(0)
                scrollState.animateScrollTo(scrollPosition)
            }
        }
    }

    // 处理替换
    LaunchedEffect(findAndReplaceState.replaceType) {
        if (findAndReplaceState.replaceType != ReplaceType.NONE && findAndReplaceState.searchWord.isNotEmpty()) {
            if (findAndReplaceState.replaceType == ReplaceType.ALL) {
                val currentText = textFieldState.text.toString()
                val newText = currentText.replace(
                    findAndReplaceState.searchWord,
                    findAndReplaceState.replaceWord
                )
                textFieldState.setTextAndPlaceCursorAtEnd(newText)
            } else if (findAndReplaceState.replaceType == ReplaceType.CURRENT) {
                if (matchedWordsRanges.isNotEmpty() && currentRangeIndex in matchedWordsRanges.indices) {
                    val (startIndex, endIndex) = matchedWordsRanges[currentRangeIndex]
                    textFieldState.edit {
                        replace(startIndex, endIndex, findAndReplaceState.replaceWord)
                    }
                }
            }
            findAndReplaceState.replaceType = ReplaceType.NONE
        }
    }

    LaunchedEffect(textLayoutResult, scrollState.isScrollInProgress) {
        val layoutResult = textLayoutResult ?: return@LaunchedEffect
        if (!scrollState.isScrollInProgress) {
            withContext(Dispatchers.Default) {
                val firstVisibleLine =
                    layoutResult.getLineForVerticalPosition(scrollState.value.toFloat())
                val firstVisibleCharPosition = layoutResult.getLineStart(firstVisibleLine)
                withContext(Dispatchers.Main) { onScroll(firstVisibleCharPosition) }
            }
        }
    }

    val actualLinePositions by remember(isLineNumberVisible) {
        derivedStateOf {
            val layoutResult = textLayoutResult
            if (!isLineNumberVisible || layoutResult == null) return@derivedStateOf emptyList()
            val lineCount = layoutResult.lineCount
            buildList(lineCount) {
                for (lineIndex in 0 until lineCount) {
                    val lineStartOffset = layoutResult.getLineStart(lineIndex)
                    // An "actual" line starts at offset 0 or is preceded by a newline character.
                    // This distinguishes from soft-wrapped lines.
                    if (lineIndex == 0 || (lineStartOffset > 0 && layoutResult.layoutInput.text[lineStartOffset - 1] == '\n')) {
                        val lineTopY = layoutResult.getLineTop(lineIndex)
                        add(lineStartOffset to lineTopY)
                    }
                }
            }
        }
    }

    val currentLines by remember(actualLinePositions) {
        derivedStateOf {
            if (actualLinePositions.isEmpty()) IntRange.EMPTY
            else {
                val selection = textFieldState.selection
                val startLine =
                    actualLinePositions.binarySearch { it.first.compareTo(selection.min) }.let {
                        if (it >= 0) it else -it - 2
                    }
                if (selection.collapsed) return@derivedStateOf IntRange(startLine, startLine)
                val endLine =
                    actualLinePositions.binarySearch { it.first.compareTo(selection.max) }.let {
                        if (it >= 0) it else -it - 2
                    }
                IntRange(startLine, endLine)
            }
        }
    }

    val lintErrors by produceState(emptyList(), lint) {
        if (lint != null) {
            snapshotFlow { textFieldState.text.toString() }
                .debounce(300L)
                .mapLatest { lint.validate(it) }
                .flowOn(Dispatchers.Default)
                .collect { value = it }
        } else {
            value = emptyList()
        }
    }

    val searchPaths by remember {
        derivedStateOf {
            val layoutResult = textLayoutResult ?: return@derivedStateOf emptyList()
            matchedWordsRanges.mapNotNull { (start, end) ->
                if (start < end && end <= layoutResult.layoutInput.text.length) {
                    layoutResult.getPathForRange(start, end)
                } else null
            }
        }
    }

    val lintPaths by remember {
        derivedStateOf {
            val layoutResult = textLayoutResult ?: return@derivedStateOf emptyList()
            lintErrors.mapNotNull { issue ->
                if (issue.startIndex < issue.endIndex && issue.endIndex <= layoutResult.layoutInput.text.length) {
                    layoutResult.getPathForRange(issue.startIndex, issue.endIndex)
                } else null
            }
        }
    }

    Row(modifier) {
        if (isLineNumberVisible) {
            EditorLineNumbers(
                currentLinesProvider = { currentLines },
                actualLinePositions = actualLinePositions,
                scrollProvider = { scrollState.value }
            )
            VerticalDivider()
        }
        Box(Modifier.fillMaxSize()) {
            BasicTextField(
                modifier = Modifier
                    .padding(
                        start = if (isLineNumberVisible) 4.dp else 16.dp,
                        end = 16.dp
                    )
                    .fillMaxSize()
                    .dragAndDropText(textFieldState)
                    .then(textFieldModifier),
                scrollState = scrollState,
                readOnly = readOnly,
                state = textFieldState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { textLayoutResult = it() },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                outputTransformation = outputTransformation,
                decorator = object : TextFieldDecorator {
                    @Composable
                    override fun Decoration(innerTextField: @Composable (() -> Unit)) {
                        Box(
                            modifier = Modifier
                                .clipToBounds()
                                .editorDrawing(
                                    searchPaths = searchPaths,
                                    currentRangeIndex = currentRangeIndex,
                                    lintPaths = lintPaths,
                                    scrollState = scrollState
                                )
                        ) {
                            if (textFieldState.text.isEmpty()) {
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
                }
            )
            if (!rememberIsScreenWidthExpanded()) {
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight(),
                    state = scrollState
                )
            }
        }
    }
}

private suspend fun findAllRanges(text: String, word: String): List<Pair<Int, Int>> {
    if (word.isEmpty() || text.isEmpty()) return emptyList()

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
    val points = List(pointCount) { i ->
        val x = bounds.left + i * sampleStep
        val y = bounds.bottom + 2f + amplitude * sin((x * (2f * PI / wavelength)) + phase).toFloat()
        Offset(x, y)
    }

    drawPoints(
        points = points,
        pointMode = PointMode.Polygon,
        color = Color.Red,
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )
}

@Composable
private fun Modifier.editorDrawing(
    searchPaths: List<Path>,
    currentRangeIndex: Int,
    lintPaths: List<Path>,
    scrollState: ScrollState
): Modifier {
    val infiniteTransition = rememberInfiniteTransition("wavy-line")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "wave-phase"
    )

    return drawWithCache {
        // 预先计算颜色，避免在绘制循环中重复创建
        val highlightBgColor = Color.Cyan.copy(alpha = 0.5f)
        val currentHighlightBorderColor = Color.Blue
        val otherHighlightBorderColor = Color.Cyan

        onDrawBehind {
            val currentScroll = scrollState.value.toFloat()
            withTransform({ translate(top = -currentScroll) }) {
                // 绘制搜索高亮
                searchPaths.forEachIndexed { index, path ->
                    drawPath(path, highlightBgColor, 0.5f)
                    val borderColor =
                        if (index == currentRangeIndex) currentHighlightBorderColor
                        else otherHighlightBorderColor
                    drawPath(path, borderColor, style = Stroke(1.sp.toPx()))
                }
                // 绘制波浪线
                lintPaths.forEach {
                    drawWavyUnderlineOptimized(it, phase)
                }
            }
        }
    }
}