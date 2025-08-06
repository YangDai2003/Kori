package kink

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

sealed interface DrawAction {
    // 表示一根完整的画笔笔触
    data class PenStroke(
        val path: Path,
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float
    ) : DrawAction

    // 表示一根高亮笔触
    data class HighLighterStroke(
        val path: Path,
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float
    ) : DrawAction

    // 表示一根橡皮擦笔触（用于部分擦除）
    data class Erase(
        val path: Path,
        val points: List<Offset>,
        val strokeWidth: Float,
        val color: Color
    ) : DrawAction
}