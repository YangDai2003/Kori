package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.DialogProperties
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.outline
import kori.composeapp.generated.resources.overview
import kori.composeapp.generated.resources.right_panel_close
import kori.composeapp.generated.resources.settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.navigation.Screen
import kotlin.math.abs
import kotlin.math.roundToInt

private val SheetWidth = 320.dp
private val ActionWidth = 48.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteSideSheet(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    showOutline: Boolean,
    outline: HeaderNode,
    onHeaderClick: (IntRange) -> Unit,
    navigateTo: (Screen) -> Unit,
    actionContent: @Composable ColumnScope.() -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit
) {
    // 仅在 isDrawerOpen 为 true 时组合 Dialog
    if (isDrawerOpen) {
        val scope = rememberCoroutineScope()

        // 由于 Dialog 有自己的生命周期，我们需要一个在 Dialog 内部触发关闭动画并最终调用 onDismiss 的方法
        // 不能直接在 onDismissRequest 中调用 onDismiss，否则动画会被打断
        var isExiting by remember { mutableStateOf(false) }

        BasicAlertDialog(
            onDismissRequest = {
                // 标记为正在退出，以防止重复触发
                if (!isExiting) {
                    isExiting = true
                    // onDismiss 将在动画结束后被调用
                }
            },
            modifier = Modifier.fillMaxSize(),
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // 允许内容填充整个屏幕
                dismissOnClickOutside = false
            )
        ) {
            val density = LocalDensity.current

            BoxWithConstraints(modifier = Modifier.fillMaxSize().clipToBounds()) {

                val drawerWidth = remember(maxWidth) {
                    val maxAllowedWidth = maxWidth - 96.dp
                    min(SheetWidth, maxAllowedWidth)
                }

                val drawerWidthPx = with(density) { drawerWidth.toPx() }
                val actionWidthPx = with(density) { ActionWidth.toPx() }
                val fullOffsetPx = drawerWidthPx + actionWidthPx

                val offsetX = remember { Animatable(fullOffsetPx) }

                // 动画退出逻辑
                val animateExit = {
                    scope.launch {
                        offsetX.animateTo(fullOffsetPx, animationSpec = tween(durationMillis = 300))
                    }.invokeOnCompletion {
                        onDismiss()
                    }
                }

                // isExiting 状态变化时触发退出动画
                LaunchedEffect(isExiting) {
                    if (isExiting) {
                        animateExit()
                    }
                }

                // 首次进入时触发打开动画
                LaunchedEffect(Unit) {
                    offsetX.animateTo(0f, animationSpec = tween(durationMillis = 300))
                }

                PredictiveBackHandler(enabled = !isExiting) { progress ->
                    try {
                        progress.collect { event ->
                            scope.launch {
                                val newOffset = fullOffsetPx * event.progress
                                offsetX.snapTo(newOffset)
                            }
                        }
                        // 预测性返回手势完成，触发退出
                        if (!isExiting) isExiting = true
                    } catch (_: CancellationException) {
                        // 手势取消，恢复到打开状态
                        scope.launch {
                            offsetX.animateTo(0f)
                        }
                    }
                }

                // 整个 Dialog 内容的根 Box
                Box(Modifier.fillMaxSize()) {

                    Box(
                        Modifier.fillMaxSize().pointerInput(Unit) {
                            detectTapGestures(onTap = { if (!isExiting) isExiting = true })
                        }
                    )

                    // 侧边操作栏 (左侧部分)
                    Column(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .offset {
                                IntOffset(
                                    x = (offsetX.value - drawerWidthPx).roundToInt(),
                                    y = 0
                                )
                            }
                            .align(Alignment.TopEnd)
                            .background(
                                color = DrawerDefaults.modalContainerColor.copy(alpha = 0.9f),
                                shape = MaterialTheme.shapes.extraLarge.copy(
                                    topEnd = CornerSize(0),
                                    bottomEnd = CornerSize(0)
                                )
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        content = actionContent
                    )

                    // 拖动状态
                    val dragState = rememberDraggableState { delta ->
                        scope.launch {
                            val newValue = (offsetX.value + delta).coerceIn(0f, fullOffsetPx)
                            offsetX.snapTo(newValue)
                        }
                    }

                    // 抽屉内容 (右侧主体)
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(drawerWidth)
                            .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                            .align(Alignment.CenterEnd)
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = dragState,
                                enabled = !isExiting,
                                onDragStopped = { velocity ->
                                    val shouldClose = if (abs(velocity) < 100) {
                                        offsetX.value > drawerWidthPx / 2
                                    } else {
                                        velocity > 0
                                    }

                                    if (shouldClose) {
                                        if (!isExiting) isExiting = true
                                    } else {
                                        // 动画恢复到打开状态
                                        scope.launch {
                                            offsetX.animateTo(0f)
                                        }
                                    }
                                }
                            ),
                        color = DrawerDefaults.modalContainerColor.copy(alpha = 0.95f),
                        shape = MaterialTheme.shapes.large.copy(
                            topEnd = CornerSize(0),
                            bottomEnd = CornerSize(0)
                        ),
                        shadowElevation = 2.dp,
                        tonalElevation = DrawerDefaults.ModalDrawerElevation
                    ) {
                        var isAllExpanded by rememberSaveable { mutableStateOf(true) }

                        LazyColumn(Modifier.fillMaxSize()) {
                            // 顶部操作按钮
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        modifier = Modifier.padding(start = 12.dp),
                                        text = stringResource(Res.string.overview),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TooltipIconButton(
                                            tipText = stringResource(Res.string.settings),
                                            icon = Icons.Outlined.Settings,
                                            onClick = {
                                                if (!isExiting) isExiting = true
                                                navigateTo(Screen.Settings)
                                            }
                                        )
                                        IconButton(onClick = { if (!isExiting) isExiting = true }) {
                                            Icon(
                                                painter = painterResource(Res.drawable.right_panel_close),
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }

                            // 详细内容
                            item {
                                SelectionContainer {
                                    Column(Modifier.padding(start = 16.dp, end = 12.dp)) {
                                        drawerContent()
                                    }
                                }
                            }

                            // ... 大纲部分 (outline) ...
                            if (outline.children.isNotEmpty() && showOutline)
                                item {
                                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.outline),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )

                                        IconButton(
                                            onClick = { isAllExpanded = !isAllExpanded }
                                        ) {
                                            Icon(
                                                imageVector = if (isAllExpanded) Icons.Outlined.UnfoldLess
                                                else Icons.Outlined.UnfoldMore,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                            if (outline.children.isNotEmpty() && showOutline)
                                items(outline.children) { header ->
                                    HeaderItem(
                                        header = header,
                                        depth = 0,
                                        onHeaderClick = onHeaderClick,
                                        parentExpanded = isAllExpanded
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

@Stable
data class HeaderNode(
    val title: String,
    val level: Int,
    val range: IntRange,
    val children: MutableList<HeaderNode> = mutableListOf()
)

@Composable
fun NoteSideSheetItem(
    key: String,
    value: String
) = Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Start
) {

    val annotatedString = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            append("$key：")
        }
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            append(value)
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge
    )
}


@Composable
private fun HeaderItem(
    header: HeaderNode,
    depth: Int,
    parentExpanded: Boolean,
    onHeaderClick: (IntRange) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(parentExpanded) {
        expanded = parentExpanded
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClick(header.range) }
            .heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (header.children.isNotEmpty()) {
            IconButton(
                modifier = Modifier
                    .padding(start = (depth * 8).dp)
                    .size(32.dp),
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropDown
                    else Icons.AutoMirrored.Filled.ArrowRight,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
            }
        } else {
            Spacer(
                modifier = Modifier
                    .padding(start = (depth * 8).dp)
                    .width(32.dp)
            )
        }

        Text(
            modifier = Modifier.padding(vertical = 2.dp),
            text = header.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge.copy(lineBreak = LineBreak.Heading),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (expanded) {
        header.children.forEach { child ->
            HeaderItem(
                header = child,
                depth = depth + 1,
                onHeaderClick = onHeaderClick,
                parentExpanded = parentExpanded
            )
        }
    }
}