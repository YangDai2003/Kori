package kink

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Pinch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.confirm
import kori.composeapp.generated.resources.draw_24px
import kori.composeapp.generated.resources.ink_eraser_24px
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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

@Composable
fun InkScreenBackground(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable @UiComposable BoxWithConstraintsScope.() -> Unit,
) {
    // --- 颜色选择 ---
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

    // --- 动画 ---
    val infiniteTransition = rememberInfiniteTransition(label = "background_blobs_transition")
    val animations = List(5) { index ->
        val duration = remember { Random.nextInt(30000, 60000) }
        infiniteTransition.animateFloat(
            initialValue = Random.nextFloat(),
            targetValue = Random.nextFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing), repeatMode = RepeatMode.Reverse
            ),
            label = "blob_anim_$index"
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(backgroundColor)) {
        val maxWidth = this.maxWidth.value
        val maxHeight = this.maxHeight.value

        // 模糊层 - 动态色块
        Box(Modifier.fillMaxSize().blur(radius = 120.dp)) {
            animations.forEachIndexed { index, anim ->
                val initialX = remember(maxWidth) { Random.nextFloat(-0.4f, 0.4f) * maxWidth }
                val initialY = remember(maxHeight) { Random.nextFloat(-0.4f, 0.4f) * maxHeight }
                val targetX = remember(maxWidth) { Random.nextFloat(-0.4f, 0.4f) * maxWidth }
                val targetY = remember(maxHeight) { Random.nextFloat(-0.4f, 0.4f) * maxHeight }
                val size = remember(maxWidth) { Random.nextFloat(0.8f, 1.4f) * maxWidth }
                val alpha = remember { Random.nextFloat(0.3f, 0.7f) }

                val x = initialX + (targetX - initialX) * anim.value
                val y = initialY + (targetY - initialY) * anim.value

                Box(
                    modifier = Modifier.offset(x.dp, y.dp).size(size.dp).alpha(alpha).background(
                        Brush.radialGradient(
                            colors = listOf(colors[index % colors.size], Color.Transparent),
                            radius = size / 2
                        )
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

@Composable
fun InkScreen(drawState: DrawState = rememberDrawState()) {
    var graphicsLayer by remember { mutableStateOf<GraphicsLayer?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    Scaffold { innerPadding ->
        InkScreenBackground {
            Box(
                modifier = Modifier.fillMaxSize()
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
                    },
                contentAlignment = Alignment.Center
            ) {
                DrawCanvas(drawState) { graphicsLayer = it }
                FloatingToolbar(
                    innerPadding = innerPadding,
                    leadingContent = {
                        ToolbarButton(
                            checked = drawState.toolMode.value == ToolMode.PEN,
                            onCheckedChange = { if (it) drawState.toolMode.value = ToolMode.PEN },
                            popupContent = { BrushStylusPane(drawState) }) {
                            Icon(
                                painter = painterResource(Res.drawable.draw_24px),
                                contentDescription = "Pen"
                            )
                        }
                        val lastSelectedEraserMode =
                            remember { mutableStateOf(ToolMode.ERASER_ENTIRE) }
                        ToolbarButton(
                            checked = drawState.toolMode.value == ToolMode.ERASER_ENTIRE || drawState.toolMode.value == ToolMode.ERASER_PARTIAL,
                            onCheckedChange = {
                                if (it) drawState.toolMode.value = lastSelectedEraserMode.value
                            },
                            popupContent = { showPopup ->
                                EraserStylusPane(lastSelectedEraserMode, drawState, showPopup)
                            }) {
                            Icon(
                                painter = painterResource(Res.drawable.ink_eraser_24px),
                                contentDescription = "Eraser"
                            )
                        }
                    },
                    trailingContent = {
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
                                    }) {
                                    Text("Reset canvas")
                                }
                            }) {
                            Icon(imageVector = Icons.Outlined.Pinch, contentDescription = "pinch")
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                imageBitmap = graphicsLayer?.toImageBitmap()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Image, contentDescription = "Image"
                            )
                        }
                    }
                )
            }
        }
    }
    imageBitmap?.let {
        AlertDialog(
            onDismissRequest = { imageBitmap = null },
            text = {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = it,
                    contentDescription = "Saved image"
                )
            },
            confirmButton = {
                TextButton(onClick = { imageBitmap = null }) {
                    Text(stringResource(Res.string.confirm))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrushStylusPane(state: DrawState) = Column(
    modifier = Modifier.width(200.dp).padding(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Brush Size:", style = MaterialTheme.typography.labelMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.clickable {
                    state.penStrokeWidth.value =
                        state.penStrokeWidth.value.minus(1).coerceAtLeast(1f)
                }, imageVector = Icons.Default.Remove, contentDescription = "reduce"
            )
            Text(
                "${state.penStrokeWidth.value.roundToInt()}",
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                modifier = Modifier.clickable {
                    state.penStrokeWidth.value =
                        state.penStrokeWidth.value.plus(1).coerceAtMost(100f)
                }, imageVector = Icons.Default.Add, contentDescription = "increase"
            )
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    Slider(
        modifier = Modifier.fillMaxWidth().height(24.dp),
        value = state.penStrokeWidth.value,
        onValueChange = {
            state.penStrokeWidth.value = it.roundToInt().toFloat()
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val colors = listOf(
            Color.Black, Color.White, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Gray
        )
        colors.forEach { color ->
            Box(
                modifier = Modifier.height(24.dp).aspectRatio(1f).clip(CircleShape)
                    .background(color).clickable(role = Role.RadioButton) {
                        state.penColor.value = color
                    }, contentAlignment = Alignment.Center
            ) {
                if (state.penColor.value == color) {
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
            showPopup.value = false
        }) {
        Text("Erase all strokes", maxLines = 1)
    }
}