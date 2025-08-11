package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface DrawAction {

    @Serializable
    data class PenStroke(
        @Transient val path: Path = Path(),
        // 使用 @Contextual，框架会自动为 List<Offset> 找到 OffsetSerializer
        override val points: List<@Contextual Offset>,
        @Contextual val color: Color,
        val strokeWidth: Float
    ) : DrawAction {
        override fun withPath(newPath: Path): DrawAction = this.copy(path = newPath)
    }

    @Serializable
    data class HighLighterStroke(
        @Transient val path: Path = Path(),
        override val points: List<@Contextual Offset>,
        @Contextual val color: Color,
        val strokeWidth: Float
    ) : DrawAction {
        override fun withPath(newPath: Path): DrawAction = this.copy(path = newPath)
    }

    @Serializable
    data class Erase(
        @Transient val path: Path = Path(),
        override val points: List<@Contextual Offset>,
        val strokeWidth: Float,
        @Contextual val color: Color
    ) : DrawAction {
        override fun withPath(newPath: Path): DrawAction = this.copy(path = newPath)
    }

    // 为所有子类提供通用的 `points` 属性，方便序列化
    val points: List<Offset>

    // 为所有子类提供一个重新构建路径后的新实例的方法
    fun withPath(newPath: Path): DrawAction
}