package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max

private class LineNumberLayoutCache(
    private val textMeasurer: TextMeasurer,
    private val style: TextStyle
) {
    private val cache = mutableMapOf<Int, TextLayoutResult>()

    fun get(lineNumber: Int): TextLayoutResult {
        return cache.getOrPut(lineNumber) {
            textMeasurer.measure(
                text = lineNumber.toString(),
                style = style,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LineNumbersColumn(
    currentLinesProvider: () -> IntRange,
    actualLinePositions: List<Pair<Int, Float>>, // line start index to line top position
    scrollProvider: () -> Int,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textMeasurer: TextMeasurer = rememberTextMeasurer()
) {

    val layoutCache = remember(textMeasurer, textStyle) {
        LineNumberLayoutCache(textMeasurer, textStyle)
    }

    val density = LocalDensity.current
    val requiredWidth by remember(actualLinePositions.size, textStyle, density) {
        derivedStateOf {
            val maxLineNumber = max(1, actualLinePositions.size)
            val maxDigitsText = "8".repeat(maxLineNumber.toString().length + 1)
            with(density) { textMeasurer.measure(maxDigitsText, textStyle).size.width.toDp() }
        }
    }

    // 用于UI的、经过防抖处理的宽度状态
    val debouncedWidth by produceState(initialValue = requiredWidth, key1 = requiredWidth) {
        if (requiredWidth > value) {
            value = requiredWidth
        } else {
            delay(200)
            value = requiredWidth
        }
    }

    Box(
        Modifier
            .fillMaxHeight()
            .width(debouncedWidth)
            .padding(horizontal = 4.dp)
            .clipToBounds()
            .drawBehind {
                if (actualLinePositions.isEmpty()) return@drawBehind

                val scrollOffset = scrollProvider().toFloat()
                val currentLines = currentLinesProvider()

                // 使用二分查找定位视口中的第一行，避免全量遍历。
                val firstVisibleLineIndex = actualLinePositions
                    .binarySearch { it.second.compareTo(scrollOffset) }
                    .let { if (it < 0) -(it + 1) else it }
                    .coerceAtMost(actualLinePositions.lastIndex)
                    // 从找到的索引往前一个开始绘制，以确保部分可见的行也能被渲染。
                    .let { (it - 1).coerceAtLeast(0) }

                // 从找到的第一个可见行开始迭代，直到超出屏幕底部。
                for (index in firstVisibleLineIndex until actualLinePositions.size) {
                    val (_, top) = actualLinePositions[index]
                    val lineY = top - scrollOffset

                    // 如果当前行已经完全在视口下方，后续所有行也都在下方，可以直接中断循环。
                    if (lineY > size.height) break

                    // 获取行高来更精确地判断是否可见
                    val textLayoutResult = layoutCache.get(index + 1)
                    val lineHeight = textLayoutResult.size.height

                    // 只绘制在视口内（或部分在视口内）的行号
                    if (lineY + lineHeight > 0) {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            color = textColor,
                            alpha = if (index in currentLines) 1.0f else 0.5f,
                            topLeft = Offset(
                                x = size.width - textLayoutResult.size.width, // 右对齐
                                y = lineY
                            )
                        )
                    }
                }
            }
    )
}
