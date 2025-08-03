package org.yangdai.kori.ink

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Pinch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ink.authoring.InProgressStrokeId
import androidx.ink.authoring.InProgressStrokesFinishedListener
import androidx.ink.authoring.InProgressStrokesView
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.ink.geometry.ImmutableBox
import androidx.ink.geometry.ImmutableVec
import androidx.ink.geometry.Vec
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import androidx.input.motionprediction.MotionEventPredictor
import org.yangdai.kori.R

@SuppressLint("ClickableViewAccessibility")
@Composable
fun DrawingView(onExit: () -> Unit) {
    val canvasStrokeRenderer = remember { CanvasStrokeRenderer.create() }
    var inProgressStrokesView by remember { mutableStateOf<InProgressStrokesView?>(null) }
    val finishedStrokesState = remember { mutableStateOf(setOf<Stroke>()) }

    var currentMode by rememberSaveable { mutableStateOf(Mode.Brush) }
    var currentBrushFamily by rememberSaveable(saver = brushFamilySaver()) {
        mutableStateOf(StockBrushes.pressurePenLatest)
    }
    val penColor = rememberSaveable { mutableLongStateOf(Color.Black.toColorLong()) }
    val penSize = rememberSaveable { mutableFloatStateOf(15f) }
    val markerColor = rememberSaveable { mutableLongStateOf(Color.Green.toColorLong()) }
    val markerSize = rememberSaveable { mutableFloatStateOf(25f) }
    val highlighterColor = rememberSaveable { mutableLongStateOf(Color.Yellow.toColorLong()) }
    val highlighterSize = rememberSaveable { mutableFloatStateOf(35f) }

    val currentBrushColor = when (currentBrushFamily) {
        StockBrushes.pressurePenLatest -> penColor.longValue
        StockBrushes.markerLatest -> markerColor.longValue
        StockBrushes.highlighterLatest -> highlighterColor.longValue
        else -> Color.Black.toColorLong()
    }
    val currentBrushSize = when (currentBrushFamily) {
        StockBrushes.pressurePenLatest -> penSize.floatValue
        StockBrushes.markerLatest -> markerSize.floatValue
        StockBrushes.highlighterLatest -> highlighterSize.floatValue
        else -> 15f
    }

    val currentBrush = remember(currentBrushFamily, currentBrushColor, currentBrushSize) {
        Brush.createWithColorIntArgb(
            family = currentBrushFamily,
            colorIntArgb = Color.fromColorLong(currentBrushColor).toArgb(),
            size = currentBrushSize,
            epsilon = 0.1F
        )
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceContainer) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentMode) {
                    if (currentMode == Mode.View) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            scale = (scale * gestureZoom).coerceIn(0.8f, 1.5f)
                            val offsetXMax = size.width / 2f
                            val offsetYMax = size.height / 2f
                            offsetX = (offsetX + pan.x).coerceIn(-offsetXMax, offsetXMax)
                            offsetY = (offsetY + pan.y).coerceIn(-offsetYMax, offsetYMax)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                        shadowElevation = 1f
                    }
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = { context ->
                        val rootView = FrameLayout(context)
                        inProgressStrokesView = InProgressStrokesView(context).apply {
                            layoutParams =
                                FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                )
                            eagerInit()
                            addFinishedStrokesListener(object : InProgressStrokesFinishedListener {
                                override fun onStrokesFinished(strokes: Map<InProgressStrokeId, Stroke>) {
                                    finishedStrokesState.value += strokes.values
                                    removeFinishedStrokes(strokes.keys)
                                }
                            })
                        }
                        rootView.addView(inProgressStrokesView)
                        rootView
                    },
                    update = { rootView ->
                        val touchListener = createTouchListener(
                            rootView = rootView,
                            inProgressStrokesView = inProgressStrokesView!!,
                            finishedStrokesState = finishedStrokesState,
                            currentMode = currentMode,
                            currentBrush = currentBrush,
                            scale = scale
                        )
                        rootView.setOnTouchListener(touchListener)
                    }
                )

                Canvas(modifier = Modifier.matchParentSize()) {
                    val canvasTransform = Matrix()
                    drawContext.canvas.nativeCanvas.concat(canvasTransform)
                    val canvas = drawContext.canvas.nativeCanvas

                    finishedStrokesState.value.forEach { stroke ->
                        canvasStrokeRenderer.draw(
                            stroke = stroke,
                            canvas = canvas,
                            strokeToScreenTransform = canvasTransform
                        )
                    }
                }
            }

            FloatingToolbar(
                innerPadding = innerPadding,
                leadingContent = {
                    ToolbarButton(
                        checked = currentBrushFamily == StockBrushes.pressurePenLatest && currentMode == Mode.Brush,
                        onCheckedChange = {
                            if (it) {
                                currentBrushFamily = StockBrushes.pressurePenLatest
                                currentMode = Mode.Brush
                            }
                        },
                        popupContent = {
                            BrushStylus(penSize, penColor)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.stylus_fountain_pen_24px),
                            contentDescription = "pressurePen",
                            tint = Color.fromColorLong(penColor.longValue)
                        )
                    }
                    ToolbarButton(
                        checked = currentBrushFamily == StockBrushes.markerLatest && currentMode == Mode.Brush,
                        onCheckedChange = {
                            if (it) {
                                currentBrushFamily = StockBrushes.markerLatest
                                currentMode = Mode.Brush
                            }
                        },
                        popupContent = {
                            BrushStylus(markerSize, markerColor)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.stylus_pen_24px),
                            contentDescription = "marker",
                            tint = Color.fromColorLong(markerColor.longValue)
                        )
                    }
                    ToolbarButton(
                        checked = currentBrushFamily == StockBrushes.highlighterLatest && currentMode == Mode.Brush,
                        onCheckedChange = {
                            if (it) {
                                currentBrushFamily = StockBrushes.highlighterLatest
                                currentMode = Mode.Brush
                            }
                        },
                        popupContent = {
                            BrushStylus(highlighterSize, highlighterColor)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.stylus_highlighter_24px),
                            contentDescription = "highlighter",
                            tint = Color.fromColorLong(highlighterColor.longValue)
                        )
                    }
                },
                trailingContent = {
                    ToolbarButton(
                        checked = currentMode == Mode.Eraser,
                        onCheckedChange = { if (it) currentMode = Mode.Eraser },
                        popupContent = { showPopup ->
                            TextButton(
                                onClick = {
                                    eraseStrokesInBox(finishedStrokesState)
                                    showPopup.value = false
                                }
                            ) {
                                Text("Erase Whole Strokes")
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ink_eraser_24px),
                            contentDescription = "eraser"
                        )
                    }
                    ToolbarButton(
                        checked = currentMode == Mode.View,
                        onCheckedChange = { if (it) currentMode = Mode.View },
                        popupContent = { showPopup ->
                            TextButton(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                    showPopup.value = false
                                }
                            ) {
                                Text("Reset Canvas")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pinch,
                            contentDescription = "pinch"
                        )
                    }
                    IconButton(onClick = onExit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "exit"
                        )
                    }
                }
            )
        }
    }
}

private fun createTouchListener(
    rootView: View,
    inProgressStrokesView: InProgressStrokesView,
    finishedStrokesState: MutableState<Set<Stroke>>,
    currentMode: Mode,
    currentBrush: Brush,
    scale: Float
): View.OnTouchListener {
    val predictor = MotionEventPredictor.newInstance(rootView)
    var currentPointerId: Int? = null
    var currentStrokeId: InProgressStrokeId? = null

    // 创建一个矩阵来表示 graphicsLayer 的变换
    val transformMatrix = Matrix().apply {
        postScale(scale, scale)
    }

    // 计算逆矩阵，用于将屏幕坐标转换回画布坐标
    val inverseMatrix = Matrix()
    transformMatrix.invert(inverseMatrix)

    return View.OnTouchListener { view, event ->
        val transformedEvent = MotionEvent.obtain(event)
        transformedEvent.transform(inverseMatrix)

        when (currentMode) {
            Mode.Brush -> {
                val predictedEvent = predictor.predict()?.also {
                    it.transform(inverseMatrix)
                }

                try {
                    when (transformedEvent.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.requestUnbufferedDispatch(transformedEvent)
                            val pointerIndex = transformedEvent.actionIndex
                            val pointerId = transformedEvent.getPointerId(pointerIndex)
                            currentPointerId = pointerId
                            // 使用变换后的事件
                            currentStrokeId = inProgressStrokesView.startStroke(
                                event = transformedEvent,
                                pointerId = pointerId,
                                brush = currentBrush
                            )
                            true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val pointerId = checkNotNull(currentPointerId)
                            val strokeId = checkNotNull(currentStrokeId)

                            for (pointerIndex in 0 until transformedEvent.pointerCount) {
                                if (transformedEvent.getPointerId(pointerIndex) != pointerId) continue
                                // 使用变换后的事件
                                inProgressStrokesView.addToStroke(
                                    transformedEvent, pointerId, strokeId, predictedEvent
                                )
                            }
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            val pointerIndex = transformedEvent.actionIndex
                            val pointerId = transformedEvent.getPointerId(pointerIndex)
                            if (pointerId == currentPointerId) {
                                val currentStrokeId = checkNotNull(currentStrokeId)
                                // 使用变换后的事件
                                inProgressStrokesView.finishStroke(
                                    transformedEvent, pointerId, currentStrokeId
                                )
                                view.performClick()
                            }
                            true
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            val pointerIndex = transformedEvent.actionIndex
                            val pointerId = transformedEvent.getPointerId(pointerIndex)
                            if (pointerId == currentPointerId) {
                                val currentStrokeId = checkNotNull(currentStrokeId)
                                // 使用变换后的事件
                                inProgressStrokesView.cancelStroke(
                                    currentStrokeId,
                                    transformedEvent
                                )
                            }
                            true
                        }

                        else -> false
                    }
                } finally {
                    predictedEvent?.recycle()
                }
            }

            Mode.Eraser -> {
                when (transformedEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val eraserSize = 25f / scale // 橡皮擦在缩放时保持视觉大小一致
                        val eraserBox = ImmutableBox.fromCenterAndDimensions(
                            ImmutableVec(transformedEvent.x, transformedEvent.y),
                            eraserSize,
                            eraserSize
                        )
                        eraseStrokesInBox(finishedStrokesState, eraserBox)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        view.performClick()
                        true
                    }

                    else -> false
                }
            }

            Mode.View -> {
                false
            }
        }
    }
}

private fun eraseStrokesInBox(
    finishedStrokesState: MutableState<Set<Stroke>>,
    eraserBox: ImmutableBox = ImmutableBox.fromCenterAndDimensions(
        Vec.ORIGIN, Float.MAX_VALUE, Float.MAX_VALUE
    )
) {
    // A stroke is considered for erasing if even a small part of it is in the box.
    val threshold = 0.01f

    val strokesToErase = finishedStrokesState.value.filter { stroke ->
        stroke.shape.computeCoverageIsGreaterThan(
            box = eraserBox,
            coverageThreshold = threshold,
        )
    }

    if (strokesToErase.isNotEmpty()) {
        Snapshot.withMutableSnapshot {
            finishedStrokesState.value = finishedStrokesState.value - strokesToErase.toSet()
        }
    }
}