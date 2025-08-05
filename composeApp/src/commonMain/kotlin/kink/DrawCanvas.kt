package kink

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.input.pointer.pointerInput
import kink.DrawAction.BrushStroke
import kink.DrawAction.Erase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// 通过上下双Canvas分离职责，实现高性能画布
@Composable
fun BoxScope.DrawCanvas(state: DrawState, onProvideGraphicsLayer: (GraphicsLayer?) -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val graphicsLayer = rememberGraphicsLayer()
    // --- 底层画布 ---
    Canvas(
        Modifier
            .matchParentSize()
            .graphicsLayer {
                translationX = state.offset.value.x
                translationY = state.offset.value.y
                scaleX = state.scale.value
                scaleY = state.scale.value
                rotationZ = state.rotation.value
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                // call record to capture the content in the graphics layer
                graphicsLayer.record {
                    // draw the contents of the composable into the graphics layer
                    this@drawWithContent.drawContent()
                }
                // draw the graphics layer on the visible canvas
                drawLayer(graphicsLayer)
                onProvideGraphicsLayer(graphicsLayer)
            }
    ) {
        drawRect(backgroundColor)
        with(drawContext) {
            canvas.withSaveLayer(size.toRect(), Paint()) {
                state.actions.forEach { action ->
                    drawAction(action, true)
                }
            }
        }
    }

    // --- 顶层画布 ---
    Canvas(
        Modifier
            .matchParentSize()
            .pointerInput(state.toolMode.value) {
                // 只有在非VIEWER模式下，才启用绘制/擦除手势
                if (state.toolMode.value != ToolMode.VIEWER) {
                    val canvasCenter = Offset(this.size.width / 2f, this.size.height / 2f)
                    detectDragGestures(
                        onDragStart = { offset ->
                            // 将屏幕坐标转换为画布的逻辑坐标
                            val transformedOffset =
                                transformPointerInput(offset, state, canvasCenter)
                            state.lastDragPosition = transformedOffset
                            state.currentPathPoints.clear()
                            state.currentPathPoints.add(transformedOffset)
                            state.indicatorPosition.value = transformedOffset

                            when (state.toolMode.value) {
                                ToolMode.PEN -> {
                                    state.inProgressPath.value = BrushStroke(
                                        path = buildPathFromPoints(state.currentPathPoints),
                                        points = state.currentPathPoints.toList(),
                                        color = state.penColor.value,
                                        strokeWidth = state.penStrokeWidth.value
                                    )
                                }

                                ToolMode.ERASER_PARTIAL -> {
                                    state.inProgressPath.value = Erase(
                                        path = buildPathFromPoints(state.currentPathPoints),
                                        points = state.currentPathPoints.toList(),
                                        strokeWidth = state.eraserStrokeWidth.value,
                                        color = backgroundColor
                                    )
                                }

                                ToolMode.ERASER_ENTIRE -> {
                                    checkAndRemoveEntirePath(state, transformedOffset)
                                }

                                ToolMode.VIEWER -> {}
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val transformedPos =
                                transformPointerInput(change.position, state, canvasCenter)
                            state.currentPathPoints.add(transformedPos)
                            val newPath = buildPathFromPoints(state.currentPathPoints)
                            state.indicatorPosition.value = transformedPos
                            when (val currentAction = state.inProgressPath.value) {
                                is BrushStroke -> {
                                    state.inProgressPath.value = currentAction.copy(
                                        path = newPath,
                                        points = state.currentPathPoints.toList()
                                    )
                                }

                                is Erase -> {
                                    state.inProgressPath.value = currentAction.copy(
                                        path = newPath,
                                        points = state.currentPathPoints.toList()
                                    )
                                }

                                else -> { // ERASER_ENTIRE 模式
                                    checkAndRemoveEntirePath(state, transformedPos)
                                }
                            }
                            state.lastDragPosition = transformedPos
                        },
                        onDragEnd = {
                            state.inProgressPath.value?.let {
                                if ((it is BrushStroke && it.points.size > 1) || (it is Erase && it.points.size > 1)) {
                                    state.actions.add(it)
                                }
                            }
                            state.inProgressPath.value = null
                            state.lastDragPosition = null
                            state.indicatorPosition.value = null
                            state.currentPathPoints.clear()
                        },
                        onDragCancel = {
                            state.inProgressPath.value = null
                            state.lastDragPosition = null
                            state.indicatorPosition.value = null
                            state.currentPathPoints.clear()
                        }
                    )
                }
            }
    ) {
        // 对顶层画布也应用同样的变换
        withTransform({
            translate(state.offset.value.x, state.offset.value.y)
            scale(state.scale.value, state.scale.value)
            rotate(state.rotation.value)
        }) {
            clipRect {
                state.inProgressPath.value?.let { drawAction(it) }
                state.indicatorPosition.value?.let { pos ->
                    when (state.toolMode.value) {
                        ToolMode.ERASER_PARTIAL, ToolMode.ERASER_ENTIRE -> {
                            drawCircle(
                                color = Color.LightGray.copy(alpha = 0.7f),
                                radius = state.eraserStrokeWidth.value / 2,
                                center = pos,
                                style = Stroke(width = 4f / state.scale.value) // 让指示器边框保持视觉上的大小不变
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

/**
 * 辅助函数：将屏幕触摸坐标转换到画布的逻辑坐标系。
 * 严格遵循与正向变换相反的运算顺序：
 * 1. 撤销平移 (Inverse Translate)
 * 2. 撤销缩放 (Inverse Scale)
 * 3. 撤销旋转 (Inverse Rotate)
 */
private fun transformPointerInput(
    inputOffset: Offset,
    state: DrawState,
    canvasCenter: Offset
): Offset {
    // 1. 撤销平移 (Inverse Translate)
    val untranslated = inputOffset - state.offset.value

    // 2. 撤销缩放 (Inverse Scale)
    // 缩放的枢轴是画布中心，所以先将坐标移到以中心为原点，缩放后再移回去
    val unscaled = (untranslated - canvasCenter) / state.scale.value + canvasCenter

    // 3. 撤销旋转 (Inverse Rotate)
    // 旋转的枢轴也是画布中心
    val pointRelativeToCenter = unscaled - canvasCenter
    // 为了进行逆旋转，使用负的角度
    val angleRad = -(state.rotation.value.toDouble() / 180.0 * PI)
    val cosAngle = cos(angleRad)
    val sinAngle = sin(angleRad)
    // 应用标准的2D逆旋转矩阵
    val rotatedX = pointRelativeToCenter.x * cosAngle - pointRelativeToCenter.y * sinAngle
    val rotatedY = pointRelativeToCenter.x * sinAngle + pointRelativeToCenter.y * cosAngle
    val rotatedPoint = Offset(rotatedX.toFloat(), rotatedY.toFloat())

    // 将旋转后的点移回原始坐标系，得到最终的逻辑坐标
    return rotatedPoint + canvasCenter
}


// 辅助函数：根据点列表构建平滑路径
fun buildPathFromPoints(points: List<Offset>): Path {
    val path = Path()
    if (points.size > 1) {
        var previousPoint = points.first()
        path.moveTo(previousPoint.x, previousPoint.y)

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            val midPoint = previousPoint.plus(currentPoint).div(2f)
            path.quadraticTo(previousPoint.x, previousPoint.y, midPoint.x, midPoint.y)
            previousPoint = currentPoint
        }
    } else if (points.isNotEmpty()) {
        val point = points.first()
        path.moveTo(point.x, point.y)
    }
    return path
}

// 绘制完成的函数
private fun DrawScope.drawAction(action: DrawAction, done: Boolean = false) {
    when (action) {
        is BrushStroke -> {
            drawPath(
                path = action.path,
                color = action.color,
                style = Stroke(
                    width = action.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        is Erase -> {
            drawPath(
                path = action.path,
                color = action.color,
                style = Stroke(
                    width = action.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = if (done) BlendMode.Clear else BlendMode.SrcOver
            )
        }
    }
}

// 碰撞检测函数
private fun checkAndRemoveEntirePath(state: DrawState, eraserPosition: Offset) {
    val pathsToRemove = mutableListOf<DrawAction>()
    state.actions.forEach { action ->
        val (pointsToCheck, strokeWidth) = when (action) {
            is BrushStroke -> action.points to action.strokeWidth
            is Erase -> action.points to action.strokeWidth
        }

        val collision = pointsToCheck.any { point ->
            val distance =
                sqrt((point.x - eraserPosition.x).pow(2) + (point.y - eraserPosition.y).pow(2))
            distance < (strokeWidth / 2 + state.eraserStrokeWidth.value / 2)
        }
        if (collision) {
            pathsToRemove.add(action)
        }
    }
    if (pathsToRemove.isNotEmpty()) {
        state.actions.removeAll(pathsToRemove)
    }
}
