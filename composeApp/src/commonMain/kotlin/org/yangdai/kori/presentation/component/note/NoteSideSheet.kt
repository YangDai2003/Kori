package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
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

private val SheetWidth = 360.dp
private val ActionWidth = 48.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteSideSheet(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    outline: HeaderNode,
    onHeaderClick: (IntRange) -> Unit,
    navigateTo: (Screen) -> Unit,
    actionContent: @Composable ColumnScope.() -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val contentWidth = maxWidth

        val drawerWidth = remember(contentWidth) {
            // 计算合适的抽屉宽度：
            // 1. 尽可能接近360dp
            // 2. 确保与屏幕宽度至少相差80dp
            val maxAllowedWidth = contentWidth - 80.dp
            min(SheetWidth, maxAllowedWidth)
        }

        val drawerWidthPx = with(density) { drawerWidth.toPx() }
        val actionWidthPx = with(density) { ActionWidth.toPx() }
        val fullOffsetPx = drawerWidthPx + actionWidthPx

        val offsetX = remember { Animatable(fullOffsetPx) }
        val maskAlpha = remember { Animatable(0f) }

        LaunchedEffect(isDrawerOpen) {
            val targetMaskAlpha = if (isDrawerOpen) 0.6f else 0f
            val targetOffsetX = if (isDrawerOpen) 0f else fullOffsetPx

            launch { maskAlpha.animateTo(targetValue = targetMaskAlpha) }
            launch { offsetX.animateTo(targetValue = targetOffsetX) }
        }

        PredictiveBackHandler(enabled = isDrawerOpen) { progress ->
            try {
                progress.collect { event ->
                    scope.launch {
                        val newOffset = drawerWidthPx * event.progress
                        offsetX.snapTo(newOffset)
                        val newAlpha = 0.6f * (1f - event.progress)
                        maskAlpha.snapTo(newAlpha)
                    }
                }
                onDismiss()
            } catch (_: CancellationException) {
                scope.launch {
                    offsetX.animateTo(0f)
                    maskAlpha.animateTo(0.6f)
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            val showMask by remember { derivedStateOf { maskAlpha.value > 0f } }
            val scrimModifier = if (showMask) Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            } else Modifier

            Canvas(modifier = Modifier.fillMaxSize().then(scrimModifier)) {
                drawRect(Color.Black, alpha = maskAlpha.value)
            }

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .offset { IntOffset(x = (offsetX.value - drawerWidthPx).roundToInt(), y = 0) }
                    .align(Alignment.TopEnd)
                    .background(
                        color = DrawerDefaults.modalContainerColor.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.large.copy(
                            topEnd = CornerSize(0),
                            bottomEnd = CornerSize(0)
                        )
                    )
                    .pointerInput(Unit) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                content = actionContent
            )

            val dragState = rememberDraggableState { delta ->
                scope.launch {
                    val newValue =
                        (offsetX.value + delta).coerceIn(0f, fullOffsetPx)
                    offsetX.snapTo(newValue)
                    val progress = 1f - (newValue / drawerWidthPx).coerceIn(0f, 1f)
                    maskAlpha.snapTo(0.6f * progress)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                    .align(Alignment.CenterEnd)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = dragState,
                        onDragStopped = { velocity ->
                            val targetValue = if (abs(velocity) < 100) {
                                if (offsetX.value > drawerWidthPx / 2) fullOffsetPx else 0f
                            } else {
                                if (velocity > 0) fullOffsetPx else 0f
                            }

                            val targetAlpha = if (targetValue == 0f) 0.6f else 0f

                            scope.launch {
                                if (targetValue == fullOffsetPx && offsetX.value < drawerWidthPx) {
                                    onDismiss()
                                }

                                launch { offsetX.animateTo(targetValue) }
                                launch { maskAlpha.animateTo(targetAlpha) }
                            }
                        }
                    ),
                color = DrawerDefaults.modalContainerColor.copy(alpha = 0.95f),
                shape = MaterialTheme.shapes.extraLarge.copy(
                    topEnd = CornerSize(0),
                    bottomEnd = CornerSize(0)
                ),
                shadowElevation = 2.dp,
                tonalElevation = DrawerDefaults.ModalDrawerElevation
            ) {
                var isAllExpanded by rememberSaveable { mutableStateOf(true) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(end = 8.dp)
                ) {
                    // 顶部操作按钮
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TooltipIconButton(
                                tipText = stringResource(Res.string.settings),
                                icon = Icons.Outlined.Settings,
                                onClick = { navigateTo(Screen.Settings) }
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    painter = painterResource(Res.drawable.right_panel_close),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    // 概览标题
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                            text = stringResource(Res.string.overview),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 详细内容
                    item {
                        SelectionContainer {
                            Column(Modifier.padding(start = 16.dp, end = 12.dp)) {
                                drawerContent()
                            }
                        }
                    }

                    // 分隔线
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // 大纲标题
                    item {
//                        if (outline.children.isNotEmpty())
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

                    // 大纲内容
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

@Stable
data class HeaderNode(
    val title: String,
    val level: Int,
    val range: IntRange,
    val children: MutableList<HeaderNode> = mutableListOf()
)

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
            .padding(start = (depth * 8).dp)
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .clickable {
                onHeaderClick(header.range)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (header.children.isNotEmpty()) {
            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = {
                    if (header.children.isNotEmpty()) {
                        expanded = !expanded
                    }
                }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropDown
                    else Icons.AutoMirrored.Filled.ArrowRight,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
            }
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }

        Text(
            text = header.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
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

