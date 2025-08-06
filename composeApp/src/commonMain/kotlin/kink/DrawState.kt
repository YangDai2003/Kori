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

// 工具模式：画笔、橡皮擦或查看器
enum class ToolMode {
    PEN,
    HIGHLIGHTER,
    ERASER_PARTIAL,
    ERASER_ENTIRE,
    VIEWER
}

enum class GridType {
    None,
    Square,
    Rule,
    Dot
}

class DrawState {
    val actions = mutableStateListOf<DrawAction>() // 存储所有已完成的绘制动作
    val undoActions = mutableStateListOf<DrawAction>() // 存储所有撤销的绘制动作
    val inProgressPath = mutableStateOf<DrawAction?>(null) // 当前正在绘制的路径
    val toolMode = mutableStateOf(ToolMode.PEN) // 当前选择的工具模式

    val penColor = mutableStateOf(Color.Black)
    val penStrokeWidth = mutableStateOf(10f)
    val highlighterColor = mutableStateOf(Color.Yellow)
    val highlighterStrokeWidth = mutableStateOf(20f)
    val eraserStrokeWidth = mutableStateOf(25f)

    // 画布变换状态
    val scale = mutableStateOf(1f)
    val offset = mutableStateOf(Offset.Zero)
    val rotation = mutableStateOf(0f)
    val canvasColor = mutableStateOf(Color.White)
    val canvasGridType = mutableStateOf(GridType.None)

    // 内部使用，用于平滑曲线
    internal var currentPathPoints = mutableListOf<Offset>()
    internal var lastDragPosition: Offset? = null

    // 在顶层画布上显示一个指示器
    val indicatorPosition = mutableStateOf<Offset?>(null)

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_POINTS = "points"
        private const val KEY_COLOR = "color"
        private const val KEY_STROKE_WIDTH = "width"
        private const val TYPE_PEN = 0
        private const val TYPE_ERASE = 1
        private const val TYPE_HIGHLIGHTER = 2
        val Saver: Saver<DrawState, Any> = listSaver(
            save = { state ->
                // 将所有状态属性分解到一个列表中
                listOf(
                    // 存储所有已完成的笔触
                    state.actions.map { action ->
                        // 将每个 DrawAction 转换为一个可序列化的 Map
                        when (action) {
                            is DrawAction.PenStroke -> mapOf(
                                KEY_TYPE to TYPE_PEN,
                                KEY_POINTS to action.points.flatMap { listOf(it.x, it.y) },
                                KEY_COLOR to action.color.toArgb(),
                                KEY_STROKE_WIDTH to action.strokeWidth
                            )

                            is DrawAction.HighLighterStroke -> mapOf(
                                KEY_TYPE to TYPE_HIGHLIGHTER,
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
                    state.rotation.value,
                    state.canvasColor.value.toArgb(),
                    state.canvasGridType.value.name,
                    state.highlighterColor.value.toArgb(),
                    state.highlighterStrokeWidth.value
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
                            TYPE_PEN -> DrawAction.PenStroke(path, points, color, strokeWidth)
                            TYPE_ERASE -> DrawAction.Erase(path, points, strokeWidth, color)
                            TYPE_HIGHLIGHTER -> DrawAction.HighLighterStroke(
                                path,
                                points,
                                color,
                                strokeWidth
                            )

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
                restoredState.canvasColor.value = Color(savedList[9] as Int)
                restoredState.canvasGridType.value = GridType.valueOf(savedList[10] as String)
                restoredState.highlighterColor.value = Color(savedList[11] as Int)
                restoredState.highlighterStrokeWidth.value = savedList[12] as Float

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