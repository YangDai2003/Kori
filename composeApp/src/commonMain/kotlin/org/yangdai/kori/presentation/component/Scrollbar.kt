package org.yangdai.kori.presentation.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
expect fun VerticalScrollbar(modifier: Modifier, state: LazyGridState)

@Composable
expect fun HorizontalScrollbar(modifier: Modifier, state: LazyListState)

@Composable
expect fun VerticalScrollbar(modifier: Modifier, state: ScrollState)

/**
 * 一个功能完善的垂直滚动条，支持拖动和点击。
 *
 * @param modifier 应用于滚动条的 Modifier。
 * @param state 与滚动内容关联的 ScrollState。
 * @param thumbWidth 滚动条滑块的宽度。
 * @param thumbMinHeight 滚动条滑块的最小高度，以确保可见性和可操作性。
 * @param thumbColor 滚动条滑块的颜色。
 * @param trackColor 滚动条轨道的颜色（如果需要）。
 * @param hideDelayMillis 自动隐藏的延迟时间（毫秒）。
 */
@Composable
fun Scrollbar(
    modifier: Modifier,
    state: ScrollState,
    thumbWidth: Dp = 8.dp,
    thumbMinHeight: Dp = 44.dp,
    thumbColor: Color = MaterialTheme.colorScheme.outline,
    trackColor: Color = Color.Transparent,
    hideDelayMillis: Long = 800L
) {
    // 如果没有可滚动的内容，则不显示滚动条
    if (state.maxValue == 0 || state.maxValue == Int.MAX_VALUE) return

    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()

    // 控制滚动条的可见性
    var isVisible by remember { mutableStateOf(false) }

    // 动画化透明度和宽度以实现平滑的淡入淡出效果
    val alpha by animateFloatAsState(if (isVisible) 1f else 0f)
    val width by animateDpAsState(if (isDragged) thumbWidth else thumbWidth / 2)

    // 使用 LaunchedEffect 监听滚动状态以控制可见性
    LaunchedEffect(state.isScrollInProgress, isDragged) {
        if (state.isScrollInProgress || isDragged) {
            isVisible = true
        } else {
            delay(hideDelayMillis)
            isVisible = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .width(thumbWidth + 8.dp) // 增加热区，方便点击
            .background(color = trackColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    scope.launch {
                        val containerHeightPx = size.height.toFloat()
                        // 目标滚动值 = (点击位置 / 轨道高度) * 最大滚动值
                        val targetScrollValue = (offset.y / containerHeightPx) * state.maxValue
                        state.animateScrollTo(targetScrollValue.toInt())
                    }
                }
            }
    ) {
        val density = LocalDensity.current
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val thumbMinHeightPx = with(density) { thumbMinHeight.toPx() }

        // 计算滑块高度
        val totalContentHeight = containerHeightPx + state.maxValue
        val thumbHeightPx = max(
            thumbMinHeightPx,
            (containerHeightPx / totalContentHeight) * containerHeightPx
        )

        // 计算滑块可以移动的总距离
        val thumbTravelDistancePx = containerHeightPx - thumbHeightPx

        // 根据当前的滚动位置计算滑块的Y轴偏移量
        val thumbOffsetYPx = if (state.maxValue > 0) {
            (state.value.toFloat() / state.maxValue) * thumbTravelDistancePx
        } else {
            0f
        }

        // --- 滑块 (Thumb) ---
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(width)
                .height(with(density) { thumbHeightPx.toDp() })
                .offset { IntOffset(x = 0, y = thumbOffsetYPx.toInt()) }
                .background(
                    color = thumbColor.copy(alpha = alpha),
                    shape = RoundedCornerShape(width / 2)
                )
                .draggable(
                    state = rememberDraggableState { delta ->
                        // 将滑块的拖动距离(delta)转换为内容的滚动距离
                        scope.launch {
                            val scrollRatio = if (thumbTravelDistancePx > 0) {
                                state.maxValue / thumbTravelDistancePx
                            } else 0f
                            state.scrollBy(delta * scrollRatio)
                        }
                    },
                    interactionSource = interactionSource,
                    orientation = Orientation.Vertical
                )
        )
    }
}