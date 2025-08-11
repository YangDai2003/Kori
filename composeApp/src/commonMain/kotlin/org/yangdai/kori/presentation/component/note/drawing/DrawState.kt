package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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

        @Serializable
        private data class SerializableDrawState(
            val actions: List<DrawAction>,
            val canvasColor: Int,
            val canvasGridType: GridType
        )

        private val json = Json {
            // 允许序列化多态类型 (sealed interface)
            serializersModule = SerializersModule {
                // 注册 DrawAction 的子类用于多态序列化
                polymorphic(DrawAction::class) {
                    subclass(DrawAction.PenStroke::class)
                    subclass(DrawAction.HighLighterStroke::class)
                    subclass(DrawAction.Erase::class)
                }
                // 为无法直接修改的外部类注册上下文序列化器
                contextual(OffsetSerializer)
                contextual(ColorSerializer)
            }
            // 为了可读性，可以开启格式化输出
            prettyPrint = true
            // 忽略 JSON 中有但数据类中没有的字段
            ignoreUnknownKeys = true
        }

        fun serializeDrawState(state: DrawState): String {
            val serializableData = SerializableDrawState(
                actions = state.actions.toList(),
                canvasColor = state.canvasColor.value.toArgb(),
                canvasGridType = state.canvasGridType.value
            )
            return json.encodeToString(serializableData)
        }

        fun deserializeDrawState(serializedState: String): DrawState {
            if (serializedState.isBlank()) return DrawState()
            return try {
                val deserializedData = json.decodeFromString<SerializableDrawState>(serializedState)

                // 重建带有 Path 的 actions
                val actionsWithPaths = deserializedData.actions.map { action ->
                    action.withPath(buildPathFromPoints(action.points))
                }

                // 创建并填充新的 DrawState
                DrawState().apply {
                    actions.addAll(actionsWithPaths)
                    canvasColor.value = Color(deserializedData.canvasColor)
                    canvasGridType.value = deserializedData.canvasGridType
                }
            } catch (e: Exception) {
                e.printStackTrace()
                DrawState()
            }
        }

        val Saver: Saver<DrawState, String> = Saver(
            save = { state -> serializeDrawState(state) },
            restore = { serializedState -> deserializeDrawState(serializedState) }
        )
    }
}

/**
 * 一个 Composable 函数，用于创建并记住一个可被自动保存和恢复的 DrawState 实例。
 */
@Composable
fun rememberDrawState(initialState: String): DrawState {
    return rememberSaveable(saver = DrawState.Saver) {
        DrawState.deserializeDrawState(initialState)
    }
}