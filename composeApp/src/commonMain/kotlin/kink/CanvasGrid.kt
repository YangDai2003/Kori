package kink

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// --- 画布背景样式 ---
private val GRID_COLOR = Color.LightGray.copy(alpha = 0.5f)
private val GRID_SPACING = 20.dp

fun DrawScope.drawSquareGrid(
    backgroundColor: Color,
    gridSpacing: Dp = GRID_SPACING,
    gridColor: Color = GRID_COLOR
) {
    drawRect(backgroundColor) // Draw background color first
    val spacing = gridSpacing.toPx()

    // Draw vertical lines
    var x = 0f
    while (x < size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height)
        )
        x += spacing
    }

    // Draw horizontal lines
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y)
        )
        y += spacing
    }
}

fun DrawScope.drawRuleGrid(
    backgroundColor: Color,
    gridSpacing: Dp = GRID_SPACING,
    gridColor: Color = GRID_COLOR
) {
    drawRect(backgroundColor) // Draw background color first
    val spacing = gridSpacing.toPx()

    // Draw horizontal lines
    var y = spacing // Start from the first line, not the edge
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y)
        )
        y += spacing
    }
}

fun DrawScope.drawDotGrid(
    backgroundColor: Color,
    gridSpacing: Dp = GRID_SPACING,
    gridColor: Color = GRID_COLOR,
    dotRadius: Dp = 1.dp // Radius of the dots
) {
    drawRect(backgroundColor) // Draw background color first
    val spacing = gridSpacing.toPx()
    val radius = dotRadius.toPx()

    var x = spacing / 2
    while (x < size.width) {
        var y = spacing / 2
        while (y < size.height) {
            drawCircle(
                color = gridColor,
                radius = radius,
                center = Offset(x, y)
            )
            y += spacing
        }
        x += spacing
    }
}