package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

// --- 画布背景样式 ---
private val GRID_COLOR = Color.LightGray.copy(alpha = 0.5f)
private const val GRID_SPACING = 20f

fun DrawScope.drawSquareGrid(
    backgroundColor: Color,
    gridSpacing: Float = GRID_SPACING,
    gridColor: Color = GRID_COLOR
) {
    drawRect(backgroundColor)

    var x = 0f
    while (x < size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height)
        )
        x += gridSpacing
    }

    var y = 0f
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y)
        )
        y += gridSpacing
    }
}

fun DrawScope.drawRuleGrid(
    backgroundColor: Color,
    gridSpacing: Float = GRID_SPACING,
    gridColor: Color = GRID_COLOR
) {
    drawRect(backgroundColor)

    var y = 0f
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y)
        )
        y += gridSpacing
    }
}

fun DrawScope.drawDotGrid(
    backgroundColor: Color,
    gridSpacing: Float = GRID_SPACING,
    gridColor: Color = GRID_COLOR
) {
    drawRect(backgroundColor)

    var x = 0f
    while (x < size.width) {
        var y = 0f
        while (y < size.height) {
            drawCircle(
                color = gridColor,
                radius = 2f,
                center = Offset(x, y)
            )
            y += gridSpacing
        }
        x += gridSpacing
    }
}