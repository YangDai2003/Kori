package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.outline
import kori.composeapp.generated.resources.overview
import kori.composeapp.generated.resources.right_panel_close
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.navigation.Screen
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteSideSheet(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    isLargeScreen: Boolean,
    outline: HeaderNode,
    onHeaderClick: (IntRange) -> Unit,
    navigateTo: (Screen) -> Unit,
    actionContent: @Composable ColumnScope.() -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit,
    animationDuration: Int = 300,
    maskColor: Color = Color.Black
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val size = with(density) { windowInfo.containerSize.toSize().toDpSize() }

    val drawerWidth = remember(size, isLargeScreen) {
        if (isLargeScreen) size.width / 3 else size.width * 2 / 3
    }

    // Width of the drawer in pixels
    val drawerWidthPx = remember(density, drawerWidth) { with(density) { drawerWidth.toPx() } }
    val actionWidthPx = remember(density) { with(density) { 48.dp.toPx() } }
    val fullOffsetPx = remember(drawerWidthPx, actionWidthPx) { drawerWidthPx + actionWidthPx }

    val offsetX = remember { Animatable(fullOffsetPx) }
    val maskAlpha = remember { Animatable(0f) }

    // Launch animations when the drawer state changes
    LaunchedEffect(isDrawerOpen) {
        val targetMaskAlpha = if (isDrawerOpen) 0.6f else 0f
        val targetOffsetX = if (isDrawerOpen) 0f else fullOffsetPx

        // Launch animations in parallel
        launch {
            maskAlpha.animateTo(
                targetValue = targetMaskAlpha,
                animationSpec = tween(durationMillis = animationDuration)
            )
        }

        launch {
            offsetX.animateTo(
                targetValue = targetOffsetX,
                animationSpec = tween(durationMillis = animationDuration)
            )
        }
    }

    PredictiveBackHandler(enabled = isDrawerOpen) { progress ->
        try {
            progress.collect { event ->
                scope.launch {
                    val newOffset = drawerWidthPx + drawerWidthPx * (event.progress - 1f)
                    offsetX.snapTo(newOffset)
                    val newAlpha = 0.6f * (1f - event.progress)
                    maskAlpha.snapTo(newAlpha)
                }
            }
            onDismiss()
        } catch (_: CancellationException) {
            offsetX.animateTo(0f)
            maskAlpha.animateTo(0f)
        }
    }

    Box(Modifier.fillMaxSize()) {

        val showMask by remember { derivedStateOf { maskAlpha.value > 0f } }
        if (showMask) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(maskColor.copy(alpha = maskAlpha.value))
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    }
            )
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

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {},
            color = DrawerDefaults.modalContainerColor.copy(alpha = 0.95f),
            shape = MaterialTheme.shapes.extraLarge.copy(
                topEnd = CornerSize(0),
                bottomEnd = CornerSize(0)
            ),
            shadowElevation = 2.dp,
            tonalElevation = DrawerDefaults.ModalDrawerElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(end = 8.dp)
            ) {
                var showDetail by rememberSaveable { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navigateTo(Screen.Settings) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Open Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(Res.drawable.right_panel_close),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Close"
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.overview),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { showDetail = !showDetail }) {
                        Icon(
                            imageVector = if (showDetail) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Visibility"
                        )
                    }
                }

                AnimatedVisibility(visible = showDetail) {
                    SelectionContainer {
                        Column(Modifier.padding(start = 16.dp, end = 12.dp)) {
                            drawerContent()
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 添加一个状态来跟踪是否全部展开
                var isAllExpanded by rememberSaveable { mutableStateOf(true) }

                if (outline.children.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
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
                                contentDescription = "fold",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                LazyColumn {
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
    value: String,
    shouldFormat: Boolean = true
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
            append("$key: ")
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
    // 当父级展开状态改变时，同步更新当前节点的展开状态
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
