package kink

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

sealed interface DrawAction {
    // 表示一根完整的画笔笔触
    data class BrushStroke(
        val path: Path,
        val points: List<Offset>, // 存储路径上的所有点
        val color: Color,
        val strokeWidth: Float
    ) : DrawAction

    // 表示一根橡皮擦笔触（用于部分擦除）
    data class Erase(
        val path: Path,
        val points: List<Offset>, // 也存储点以实现实时绘制
        val strokeWidth: Float,
        val color: Color
    ) : DrawAction
}