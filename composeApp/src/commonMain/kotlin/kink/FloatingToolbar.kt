package kink

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

interface FloatingToolbarScope {
    @Composable
    fun ToolbarButton(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        popupContent: @Composable (MutableState<Boolean>) -> Unit = {},
        content: @Composable () -> Unit
    )
}

private class FloatingToolbarScopeImpl(
    private val toolbarAlignment: Alignment
) : FloatingToolbarScope {

    @Composable
    override fun ToolbarButton(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        popupContent: @Composable (MutableState<Boolean>) -> Unit,
        content: @Composable () -> Unit
    ) {
        val showPopup = remember { mutableStateOf(false) }
        val density = LocalDensity.current

        Box {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (checked) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    contentColor = if (checked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                onClick = {
                    if (checked) showPopup.value = !showPopup.value
                    else onCheckedChange(true)
                },
                content = content
            )

            LaunchedEffect(checked) {
                if (!checked) showPopup.value = false
            }

            if (showPopup.value) {
                val offset = with(density) { 56.dp.toPx().roundToInt() }
                val popupAlignment: Alignment
                val popupOffset: IntOffset

                when (toolbarAlignment) {
                    Alignment.TopCenter -> {
                        popupAlignment = Alignment.TopCenter
                        popupOffset = IntOffset(0, offset)
                    }

                    Alignment.BottomCenter -> {
                        popupAlignment = Alignment.BottomCenter
                        popupOffset = IntOffset(0, -offset)
                    }

                    Alignment.CenterStart -> {
                        popupAlignment = Alignment.CenterStart
                        popupOffset = IntOffset(offset, 0)
                    }

                    Alignment.CenterEnd -> {
                        popupAlignment = Alignment.CenterEnd
                        popupOffset = IntOffset(-offset, 0)
                    }

                    else -> {
                        popupAlignment = Alignment.Center
                        popupOffset = IntOffset(0, 0)
                    }
                }

                Popup(
                    alignment = popupAlignment,
                    offset = popupOffset,
                    properties = PopupProperties(dismissOnClickOutside = false),
                    onDismissRequest = { showPopup.value = false }
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shadowElevation = 4.dp
                    ) {
                        popupContent(showPopup)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxWithConstraintsScope.FloatingToolbar(
    innerPadding: PaddingValues,
    leadingContent: @Composable FloatingToolbarScope.() -> Unit,
    trailingContent: @Composable FloatingToolbarScope.() -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    // 当前工具栏的最终对齐位置
    val initAlignment = if (maxWidth > maxHeight) Alignment.CenterStart else Alignment.TopCenter
    var alignment by remember { mutableStateOf(initAlignment) }
    // 在拖拽过程中，预测的对齐位置
    var predictedAlignment by remember { mutableStateOf<Alignment?>(null) }
    val density = LocalDensity.current

    val maxWidthPx = with(density) { maxWidth.toPx() }
    val maxHeightPx = with(density) { maxHeight.toPx() }

    // 将锚点位置缓存起来，避免重复计算
    val anchorPoints = remember(maxWidthPx, maxHeightPx) {
        mapOf(
            Alignment.TopCenter to Offset(maxWidthPx / 2, 0f),
            Alignment.BottomCenter to Offset(maxWidthPx / 2, maxHeightPx),
            Alignment.CenterStart to Offset(0f, maxHeightPx / 2),
            Alignment.CenterEnd to Offset(maxWidthPx, maxHeightPx / 2)
        )
    }

    // 绘制吸附位置的预览 (仅在拖拽时显示)
    predictedAlignment?.let { snapAlignment ->
        val isHorizontal =
            snapAlignment == Alignment.TopCenter || snapAlignment == Alignment.BottomCenter
        // 预览占位符
        Spacer(
            Modifier
                .align(snapAlignment)
                .padding(innerPadding)
                .padding(if (snapAlignment == Alignment.TopCenter) 0.dp else 16.dp)
                // 使用一个近似的尺寸来模拟工具栏的大小
                .size(
                    width = if (isHorizontal) 220.dp else 48.dp,
                    height = if (isHorizontal) 48.dp else 220.dp
                )
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), CircleShape)
        )
    }

    val scope = remember(alignment) { FloatingToolbarScopeImpl(alignment) }

    Box(
        modifier = Modifier
            .align(alignment)
            .padding(innerPadding)
            .padding(if (alignment == Alignment.TopCenter) 0.dp else 16.dp)
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // 拖拽结束，将最终位置确定为预测的位置
                        predictedAlignment?.let {
                            alignment = it
                        }
                        // 重置偏移量和预测状态
                        offset = Offset.Zero
                        predictedAlignment = null
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offset += dragAmount

                    // 拖拽过程中，实时计算预测的吸附位置
                    val startCenter = anchorPoints[alignment]
                        ?: Offset(maxWidthPx / 2, maxHeightPx / 2)
                    val currentDragPosition = startCenter + offset

                    predictedAlignment = anchorPoints.minByOrNull {
                        (currentDragPosition - it.value).getDistanceSquared()
                    }?.key
                }
            }
    ) {
        when (alignment) {
            Alignment.TopCenter, Alignment.BottomCenter -> {
                HorizontalFloatingToolbar(
                    modifier = Modifier.height(48.dp),
                    expanded = expanded,
                    contentPadding = PaddingValues(0.dp),
                    leadingContent = { scope.leadingContent() },
                    trailingContent = { scope.trailingContent() },
                    expandedShadowElevation = 4.dp,
                    collapsedShadowElevation = 4.dp,
                    content = {
                        FilledIconButton(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(
                                    IconButtonDefaults.extraSmallContainerSize(
                                        widthOption = IconButtonDefaults.IconButtonWidthOption.Wide
                                    )
                                ),
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(IconButtonDefaults.extraSmallIconSize)
                                    .rotate(90f),
                                imageVector = if (expanded) Icons.Default.Compress else Icons.Default.Expand,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            else -> { // CenterStart, CenterEnd
                VerticalFloatingToolbar(
                    modifier = Modifier.width(48.dp),
                    expanded = expanded,
                    contentPadding = PaddingValues(0.dp),
                    leadingContent = { scope.leadingContent() },
                    trailingContent = { scope.trailingContent() },
                    expandedShadowElevation = 4.dp,
                    collapsedShadowElevation = 4.dp,
                    content = {
                        FilledIconButton(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(
                                    IconButtonDefaults.extraSmallContainerSize(
                                        widthOption = IconButtonDefaults.IconButtonWidthOption.Narrow
                                    )
                                ),
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                                imageVector = if (expanded) Icons.Default.Compress else Icons.Default.Expand,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}