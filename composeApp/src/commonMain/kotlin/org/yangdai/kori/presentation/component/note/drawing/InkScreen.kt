package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pinch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.ink_eraser_24px
import kori.composeapp.generated.resources.stylus_highlighter_24px
import kori.composeapp.generated.resources.stylus_pen_24px
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.yangdai.kori.presentation.component.dialog.DismissButton
import org.yangdai.kori.presentation.component.dialog.dialogShape
import kotlin.math.roundToInt
import kotlin.random.Random

private val lightBrandColor1 = Color(0xFFAECBFA)
private val lightBrandColor2 = Color(0xFFD7C7EB)
private val lightBrandColor3 = Color(0xFFF0B5B9)
private val lightBackgroundColor = Color(0xFFF8F9FA)

private val darkBrandColor1 = Color(0xFF4285F4)
private val darkBrandColor2 = Color(0xFF9B72CB)
private val darkBrandColor3 = Color(0xFFD96570)
private val darkBackgroundColor = Color(0xFF2B2D30)

private data class BlobConfig(
    val color: Color,
    val initialX: Float,
    val targetX: Float,
    val initialY: Float,
    val targetY: Float,
    val initialSize: Float,
    val targetSize: Float,
    val xAnimDuration: Int,
    val yAnimDuration: Int,
    val sizeAnimDuration: Int,
    val initialAlpha: Float
)

@Composable
private fun InkScreenBackground(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    blobCount: Int = 5,
    minBlobSizeRatio: Float = 1f,
    maxBlobSizeRatio: Float = 4f,
    content: @Composable @UiComposable BoxWithConstraintsScope.() -> Unit
) {
    val colors = remember(isDarkTheme) {
        if (isDarkTheme) listOf(
            darkBrandColor1,
            darkBrandColor2,
            darkBrandColor3,
            darkBrandColor1.copy(alpha = 0.7f),
            darkBrandColor2.copy(alpha = 0.7f)
        )
        else listOf(
            lightBrandColor1,
            lightBrandColor2,
            lightBrandColor3,
            lightBrandColor1.copy(alpha = 0.8f),
            lightBrandColor2.copy(alpha = 0.8f)
        )
    }
    val backgroundColor = if (isDarkTheme) darkBackgroundColor else lightBackgroundColor

    BoxWithConstraints(Modifier.fillMaxSize().background(backgroundColor)) {
        val maxWidth = this.maxWidth.value
        val maxHeight = this.maxHeight.value

        val blobConfigs = remember(maxWidth, maxHeight, blobCount, colors) {
            List(blobCount) { index ->
                val minSize = minBlobSizeRatio * maxWidth
                val maxSize = maxBlobSizeRatio * maxWidth
                BlobConfig(
                    color = colors[index % colors.size],
                    initialX = Random.nextFloat(-0.4f, 0.4f) * maxWidth,
                    targetX = Random.nextFloat(-0.4f, 0.4f) * maxWidth,
                    initialY = Random.nextFloat(-0.4f, 0.4f) * maxHeight,
                    targetY = Random.nextFloat(-0.4f, 0.4f) * maxHeight,
                    initialSize = Random.nextFloat(minSize, maxSize),
                    targetSize = Random.nextFloat(minSize, maxSize),
                    xAnimDuration = Random.nextInt(25000, 50000),
                    yAnimDuration = Random.nextInt(25000, 50000),
                    sizeAnimDuration = Random.nextInt(30000, 60000),
                    initialAlpha = Random.nextFloat(0.1f, 0.4f)
                )
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "background_blobs_transition")

        // 模糊层 - 动态色块
        Box(Modifier.fillMaxSize()) {
            blobConfigs.forEachIndexed { index, config ->
                // X 轴动画
                val xAnim by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(config.xAnimDuration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "blob_x_anim_$index"
                )
                // Y 轴动画
                val yAnim by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(config.yAnimDuration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "blob_y_anim_$index"
                )
                // 尺寸动画
                val sizeAnim by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(config.sizeAnimDuration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "blob_size_anim_$index"
                )

                // 使用动画值计算当前状态
                val x = config.initialX + (config.targetX - config.initialX) * xAnim
                val y = config.initialY + (config.targetY - config.initialY) * yAnim
                val size = config.initialSize + (config.targetSize - config.initialSize) * sizeAnim

                Box(
                    Modifier
                        .offset(x.dp, y.dp)
                        .requiredSize(size.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    config.color,
                                    config.color.copy(alpha = 0.5f),
                                    Color.Transparent
                                ),
                                radius = size / 2
                            ),
                            shape = CircleShape,
                            alpha = config.initialAlpha
                        )
                )
            }
        }
        content()
    }
}

private fun Random.nextFloat(from: Float, until: Float): Float {
    return from + nextFloat() * (until - from)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun InkScreen(
    drawState: DrawState,
    noteUuid: String,
    imageBitmap: MutableState<ImageBitmap?>,
    onDismiss: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    BackHandler {
        coroutineScope.launch {
            imageBitmap.value = graphicsLayer.toImageBitmap()
        }.invokeOnCompletion {
            onDismiss()
        }
    }
    SaveBitmapToFileOnDispose(imageBitmap.value, noteUuid)
    Scaffold(
        modifier = Modifier
            .pointerInput(drawState.toolMode.value) {
                if (drawState.toolMode.value == ToolMode.VIEWER) {
                    detectTransformGestures { _, pan, zoom, gestureRotation ->
                        val newScale = (drawState.scale.value * zoom).coerceIn(0.5f, 2f)
                        val maxOffsetX = size.width * drawState.scale.value / 2f
                        val maxOffsetY = size.height * drawState.scale.value / 2f
                        drawState.offset.value = (drawState.offset.value + pan).let {
                            Offset(
                                it.x.coerceIn(-maxOffsetX, maxOffsetX),
                                it.y.coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        }
                        drawState.scale.value = newScale
                        drawState.rotation.value += gestureRotation
                    }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.keyboardModifiers.isCtrlPressed && event.type == PointerEventType.Scroll) {
                            drawState.scale.value =
                                (drawState.scale.value - event.changes.first().scrollDelta.y / 10f)
                                    .coerceIn(0.5f, 2f)
                        } else if (event.type == PointerEventType.Scroll) {
                            drawState.rotation.value -= event.changes.first().scrollDelta.y
                        }
                    }
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.isCtrlPressed && keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Z -> {
                            if (drawState.actions.isNotEmpty()) {
                                drawState.undoActions.add(drawState.actions.last())
                                drawState.actions.removeLast()
                            }
                        }

                        Key.Y -> {
                            if (drawState.undoActions.isNotEmpty()) {
                                drawState.actions.add(drawState.undoActions.last())
                                drawState.undoActions.removeLast()
                            }
                        }

                        else -> false
                    }
                }
                false
            },
        topBar = {
            Row(
                modifier = Modifier.statusBarsPadding().fillMaxWidth().height(52.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.5f
                        )
                    ),
                    onClick = {
                        coroutineScope.launch {
                            imageBitmap.value = graphicsLayer.toImageBitmap()
                        }.invokeOnCompletion {
                            onDismiss()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = IconButtonDefaults.outlinedIconButtonBorder(true)
                ) {
                    Row(modifier = Modifier.height(40.dp)) {
                        IconButton(
                            modifier = Modifier.size(
                                IconButtonDefaults.smallContainerSize(
                                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide
                                )
                            ),
                            enabled = drawState.actions.isNotEmpty(),
                            onClick = {
                                drawState.undoActions.add(drawState.actions.last())
                                drawState.actions.removeLast()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo"
                            )
                        }
                        VerticalDivider(Modifier.padding(vertical = 4.dp))
                        IconButton(
                            modifier = Modifier.size(
                                IconButtonDefaults.smallContainerSize(
                                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide
                                )
                            ),
                            enabled = drawState.undoActions.isNotEmpty(),
                            onClick = {
                                drawState.actions.add(drawState.undoActions.last())
                                drawState.undoActions.removeLast()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo"
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Box {
                    var showMenu by remember { mutableStateOf(false) }
                    OutlinedIconButton(
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.5f
                            )
                        ),
                        onClick = { showMenu = !showMenu }
                    ) {
                        Icon(imageVector = Icons.Outlined.Palette, contentDescription = "Palette")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        DropdownMenuItem(
                            text = { Text("White") },
                            trailingIcon = {
                                RadioButton(
                                    selected = drawState.canvasColor.value == Color.White,
                                    onClick = null
                                )
                            },
                            onClick = {
                                drawState.canvasColor.value = Color.White
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Black") },
                            trailingIcon = {
                                RadioButton(
                                    selected = drawState.canvasColor.value == Color.Black,
                                    onClick = null
                                )
                            },
                            onClick = {
                                drawState.canvasColor.value = Color.Black
                                showMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("None") },
                            trailingIcon = {
                                RadioButton(
                                    selected = drawState.canvasGridType.value == GridType.None,
                                    onClick = null
                                )
                            },
                            onClick = {
                                drawState.canvasGridType.value = GridType.None
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Square") },
                            trailingIcon = {
                                RadioButton(
                                    selected = drawState.canvasGridType.value == GridType.Square,
                                    onClick = null
                                )
                            },
                            onClick = {
                                drawState.canvasGridType.value = GridType.Square
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rule") },
                            trailingIcon = {
                                RadioButton(
                                    selected = drawState.canvasGridType.value == GridType.Rule,
                                    onClick = null
                                )
                            },
                            onClick = {
                                drawState.canvasGridType.value = GridType.Rule
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dot") },
                            trailingIcon = {
                                RadioButton(
                                    selected = drawState.canvasGridType.value == GridType.Dot,
                                    onClick = null
                                )
                            },
                            onClick = {
                                drawState.canvasGridType.value = GridType.Dot
                                showMenu = false
                            }
                        )
                    }
                }
                OutlinedIconButton(
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    onClick = {
                        coroutineScope.launch {
                            imageBitmap.value = graphicsLayer.toImageBitmap()
                        }.invokeOnCompletion {
                            showDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image, contentDescription = "Image"
                    )
                }
            }
        }
    ) { innerPadding ->
        InkScreenBackground {
            DrawCanvas(drawState, graphicsLayer)
            FloatingToolbar(
                innerPadding = innerPadding,
                leadingContent = {
                    ToolbarButton(
                        checked = drawState.toolMode.value == ToolMode.PEN,
                        onCheckedChange = { if (it) drawState.toolMode.value = ToolMode.PEN },
                        popupContent = {
                            BrushStylusPane(
                                drawState.penStrokeWidth,
                                drawState.penColor
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.stylus_pen_24px),
                            contentDescription = "Pen"
                        )
                    }
                    ToolbarButton(
                        checked = drawState.toolMode.value == ToolMode.HIGHLIGHTER,
                        onCheckedChange = {
                            if (it) drawState.toolMode.value = ToolMode.HIGHLIGHTER
                        },
                        popupContent = {
                            BrushStylusPane(
                                drawState.highlighterStrokeWidth,
                                drawState.highlighterColor
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.stylus_highlighter_24px),
                            contentDescription = "Highlighter"
                        )
                    }
                },
                trailingContent = {
                    val lastSelectedEraserMode =
                        remember { mutableStateOf(ToolMode.ERASER_ENTIRE) }
                    ToolbarButton(
                        checked = drawState.toolMode.value == ToolMode.ERASER_ENTIRE || drawState.toolMode.value == ToolMode.ERASER_PARTIAL,
                        onCheckedChange = {
                            if (it) drawState.toolMode.value = lastSelectedEraserMode.value
                        },
                        popupContent = { showPopup ->
                            EraserStylusPane(lastSelectedEraserMode, drawState, showPopup)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ink_eraser_24px),
                            contentDescription = "Eraser"
                        )
                    }
                    ToolbarButton(
                        checked = drawState.toolMode.value == ToolMode.VIEWER,
                        onCheckedChange = {
                            if (it) drawState.toolMode.value = ToolMode.VIEWER
                        },
                        popupContent = { showPopup ->
                            TextButton(
                                onClick = {
                                    drawState.scale.value = 1f
                                    drawState.offset.value = Offset.Zero
                                    drawState.rotation.value = 0f
                                    showPopup.value = false
                                }
                            ) {
                                Text("Reset canvas")
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Outlined.Pinch, contentDescription = "pinch")
                    }
                }
            )
        }
    }

    if (showDialog) {
        imageBitmap.value?.let {
            AlertDialog(
                shape = dialogShape(),
                onDismissRequest = { showDialog = false },
                text = {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        bitmap = it,
                        contentDescription = "Canvas image"
                    )
                },
                confirmButton = { ShareImageButton(it) },
                dismissButton = { DismissButton { showDialog = false } }
            )
        }
    }
}

@Composable
expect fun ShareImageButton(imageBitmap: ImageBitmap)

@Composable
expect fun SaveBitmapToFileOnDispose(imageBitmap: ImageBitmap?, uuid: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrushStylusPane(widthState: MutableState<Float>, colorState: MutableState<Color>) =
    Column(
        modifier = Modifier.width(200.dp).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Brush Size:", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.clickable {
                        widthState.value = widthState.value.minus(1).coerceAtLeast(1f)
                    },
                    imageVector = Icons.Default.Remove, contentDescription = "reduce"
                )
                Text(
                    "${widthState.value.roundToInt()}",
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    modifier = Modifier.clickable {
                        widthState.value = widthState.value.plus(1).coerceAtMost(100f)
                    },
                    imageVector = Icons.Default.Add, contentDescription = "increase"
                )
            }
        }
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            value = widthState.value,
            onValueChange = {
                widthState.value = it.roundToInt().toFloat()
            },
            valueRange = 1f..100f,
            steps = 99,
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource, thumbSize = DpSize(4.dp, 24.dp)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    modifier = Modifier.height(8.dp),
                    sliderState = sliderState,
                    drawStopIndicator = {},
                    drawTick = { _, _ -> },
                    thumbTrackGapSize = 4.dp
                )
            })
        Row(
            modifier = Modifier.padding(horizontal = 4.dp).padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Brush Color:",
                style = MaterialTheme.typography.labelMedium
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            maxItemsInEachRow = 7
        ) {
            val colors = listOf(
                // 基本色
                Color.Black,
                Color.White,
                Color.Red,
                Color.Yellow,
                Color.Blue,
                Color.Green,
                Color.Gray,
                Color.Magenta,
                // 鲜艳色
                Color(0xFFFFA500), // Orange
                Color(0xFF800080), // Purple
                Color(0xFFFFC0CB), // Pink
                Color(0xFF00FFFF), // Cyan
                Color(0xFFFA8072), // Salmon
                Color(0xFFADFF2F), // GreenYellow
                Color(0xFFFFD700), // Gold
                // 柔和色
                Color(0xFFADD8E6), // Light Blue
                Color(0xFFFFFFE0), // Light Yellow
                Color(0xFFE6E6FA), // Lavender
                Color(0xFF90EE90), // LightGreen
                // 暗色
                Color(0xFF008000), // Dark Green
                Color(0xFFA52A2A), // Brown
            )
            colors.forEach { color ->
                Box(
                    modifier = Modifier.height(24.dp).aspectRatio(1f).clip(CircleShape)
                        .background(color)
                        .clickable(role = Role.RadioButton) {
                            colorState.value = color
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (colorState.value == color) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (color == Color.Black) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EraserStylusPane(
    lastSelectedEraserMode: MutableState<ToolMode>,
    state: DrawState,
    showPopup: MutableState<Boolean>
) = Column(
    modifier = Modifier.width(200.dp).padding(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 4.dp).selectable(
            selected = state.toolMode.value == ToolMode.ERASER_ENTIRE, onClick = {
                state.toolMode.value = ToolMode.ERASER_ENTIRE
                lastSelectedEraserMode.value = ToolMode.ERASER_ENTIRE
            }, role = Role.RadioButton
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Stroke eraser", style = MaterialTheme.typography.labelMedium)
        RadioButton(
            selected = state.toolMode.value == ToolMode.ERASER_ENTIRE, onClick = null
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 4.dp).selectable(
            selected = state.toolMode.value == ToolMode.ERASER_PARTIAL, onClick = {
                state.toolMode.value = ToolMode.ERASER_PARTIAL
                lastSelectedEraserMode.value = ToolMode.ERASER_PARTIAL
            }, role = Role.RadioButton
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Area eraser", style = MaterialTheme.typography.labelMedium)
        RadioButton(
            selected = state.toolMode.value == ToolMode.ERASER_PARTIAL, onClick = null
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Eraser Size:", style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.clickable {
                    state.eraserStrokeWidth.value =
                        state.eraserStrokeWidth.value.minus(1).coerceAtLeast(1f)
                }, imageVector = Icons.Default.Remove, contentDescription = "reduce"
            )
            Text(
                "${state.eraserStrokeWidth.value.roundToInt()}",
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                modifier = Modifier.clickable {
                    state.eraserStrokeWidth.value =
                        state.eraserStrokeWidth.value.plus(1).coerceAtMost(100f)
                }, imageVector = Icons.Default.Add, contentDescription = "increase"
            )
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    Slider(
        modifier = Modifier.height(24.dp),
        value = state.eraserStrokeWidth.value,
        onValueChange = {
            state.eraserStrokeWidth.value = it.roundToInt().toFloat()
        },
        valueRange = 1f..100f,
        steps = 99,
        interactionSource = interactionSource,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource, thumbSize = DpSize(4.dp, 24.dp)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                modifier = Modifier.height(8.dp),
                sliderState = sliderState,
                drawStopIndicator = {},
                drawTick = { _, _ -> },
                thumbTrackGapSize = 4.dp
            )
        })
    TextButton(
        onClick = {
            state.actions.clear()
            state.undoActions.clear()
            showPopup.value = false
        }) {
        Text("Erase all strokes", maxLines = 1)
    }
}