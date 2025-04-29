package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun LineNumbersColumn(
    currentLine: Int,
    actualLinePositions: List<Pair<Int, Float>>, // line start index to line top position
    scrollProvider: () -> Int
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxDigits by remember(actualLinePositions.size) {
        derivedStateOf {
            // Use size + 1 for max digits to handle potential padding/alignment nicely
            max(1, actualLinePositions.size).toString().length + 1
        }
    }

    val columnWidth = remember(maxDigits, textStyle) {
        val maxLineNumber = "9".repeat(maxDigits)
        val measureResult = textMeasurer.measure(
            text = maxLineNumber,
            style = textStyle,
            maxLines = 1
        )
        with(density) { measureResult.size.width.toDp() }
    }

    val lineLayoutsCache = remember { mutableMapOf<String, TextLayoutResult>() }
    var canvasHeightPx by remember { mutableIntStateOf(0) }

    Box(
        Modifier
            .width(columnWidth)
            .padding(horizontal = 4.dp)
            .fillMaxHeight()
            .clipToBounds()
            .onSizeChanged { canvasHeightPx = it.height },
        contentAlignment = Alignment.TopEnd
    ) {
        Canvas(modifier = Modifier.fillMaxHeight()) {
            val scrollOffset = scrollProvider()
            val visibleEnd = scrollOffset + canvasHeightPx

            // Only draw line numbers that are visible in the viewport, measuring on demand.
            actualLinePositions.forEachIndexed { index, (_, top) ->
                val lineY = top - scrollOffset

                if (lineY >= -50 && lineY <= visibleEnd) {
                    val lineNumber = (index + 1).toString()
                    // Lazy measure: use cached result if available, otherwise measure and cache it.
                    val textLayoutResult = lineLayoutsCache.getOrPut(lineNumber) {
                        textMeasurer.measure(
                            text = lineNumber,
                            style = textStyle,
                            maxLines = 1
                        )
                    }

                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = textColor,
                        alpha = if (currentLine == index) 1f else 0.5f,
                        topLeft = Offset(
                            x = size.width - textLayoutResult.size.width,
                            y = lineY
                        )
                    )
                }
            }
        }
    }
}
