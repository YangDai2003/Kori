package kink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class DrawState {
    val actions = mutableStateListOf<DrawAction>() // 存储所有已完成的绘制动作
    var inProgressPath = mutableStateOf<DrawAction?>(null) // 当前正在绘制的路径
    var toolMode = mutableStateOf(ToolMode.PEN) // 当前选择的工具模式

    var penColor = mutableStateOf(Color.Black)
    var penStrokeWidth = mutableStateOf(10f)
    var eraserStrokeWidth = mutableStateOf(25f)

    // 画布变换状态
    var scale = mutableStateOf(1f)
    var offset = mutableStateOf(Offset.Zero)
    var rotation = mutableStateOf(0f)

    // 内部使用，用于平滑曲线
    internal var currentPathPoints = mutableListOf<Offset>()
    internal var lastDragPosition: Offset? = null

    // 在顶层画布上显示一个指示器
    var indicatorPosition = mutableStateOf<Offset?>(null)

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_POINTS = "points"
        private const val KEY_COLOR = "color"
        private const val KEY_STROKE_WIDTH = "width"
        private const val TYPE_BRUSH = 0
        private const val TYPE_ERASE = 1
        val Saver: Saver<DrawState, Any> = listSaver(
            save = { state ->
                // 将所有状态属性分解到一个列表中
                listOf(
                    // 存储所有已完成的笔触
                    state.actions.map { action ->
                        // 将每个 DrawAction 转换为一个可序列化的 Map
                        when (action) {
                            is DrawAction.BrushStroke -> mapOf(
                                KEY_TYPE to TYPE_BRUSH,
                                KEY_POINTS to action.points.flatMap { listOf(it.x, it.y) },
                                KEY_COLOR to action.color.toArgb(),
                                KEY_STROKE_WIDTH to action.strokeWidth
                            )

                            is DrawAction.Erase -> mapOf(
                                KEY_TYPE to TYPE_ERASE,
                                KEY_POINTS to action.points.flatMap { listOf(it.x, it.y) },
                                KEY_COLOR to action.color.toArgb(),
                                KEY_STROKE_WIDTH to action.strokeWidth
                            )
                        }
                    },
                    state.toolMode.value.name,
                    state.penColor.value.toArgb(),
                    state.penStrokeWidth.value,
                    state.eraserStrokeWidth.value,
                    state.scale.value,
                    state.offset.value.x,
                    state.offset.value.y,
                    state.rotation.value
                )
            },
            restore = { savedList ->
                // 从列表中按索引恢复状态
                val restoredState = DrawState()
                // 恢复笔触列表
                @Suppress("UNCHECKED_CAST") val savedActions =
                    savedList[0] as List<Map<String, Any>>
                restoredState.actions.addAll(
                    savedActions.map { actionMap ->
                        @Suppress("UNCHECKED_CAST") val points =
                            (actionMap[KEY_POINTS] as List<Float>).chunked(2)
                                .map { Offset(it[0], it[1]) }
                        // 使用与绘制时相同的函数来重建路径，确保外观一致！
                        val path = buildPathFromPoints(points)
                        val strokeWidth = (actionMap[KEY_STROKE_WIDTH] as Number).toFloat()
                        val color = Color(actionMap[KEY_COLOR] as Int)

                        when (actionMap[KEY_TYPE] as Int) {
                            TYPE_BRUSH -> DrawAction.BrushStroke(path, points, color, strokeWidth)
                            TYPE_ERASE -> DrawAction.Erase(path, points, strokeWidth, color)
                            else -> throw IllegalStateException("Unknown action type")
                        }
                    }
                )

                // 恢复其他简单状态
                restoredState.toolMode.value = ToolMode.valueOf(savedList[1] as String)
                restoredState.penColor.value = Color(savedList[2] as Int)
                restoredState.penStrokeWidth.value = savedList[3] as Float
                restoredState.eraserStrokeWidth.value = savedList[4] as Float
                restoredState.scale.value = savedList[5] as Float
                restoredState.offset.value = Offset(savedList[6] as Float, savedList[7] as Float)
                restoredState.rotation.value = savedList[8] as Float

                restoredState
            }
        )
    }
}

/**
 * 一个 Composable 函数，用于创建并记住一个可被自动保存和恢复的 DrawState 实例。
 */
@Composable
fun rememberDrawState(): DrawState {
    return rememberSaveable(saver = DrawState.Saver) {
        DrawState()
    }
}