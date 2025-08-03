package org.yangdai.kori.ink

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toColorLong
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
    val finishedStrokesState =
        rememberSaveable(saver = strokeSetSaver()) { mutableStateOf(setOf()) }

    var currentMode by rememberSaveable { mutableStateOf(Mode.Brush) }
    var currentBrushFamily by rememberSaveable(saver = brushFamilySaver()) {
        mutableStateOf(StockBrushes.pressurePenLatest)
    }
    val penColor = rememberSaveable { mutableLongStateOf(Color.Black.toColorLong()) }
    val penSize = rememberSaveable { mutableFloatStateOf(15f) }
    val markerColor = rememberSaveable { mutableLongStateOf(Color.Black.toColorLong()) }
    val markerSize = rememberSaveable { mutableFloatStateOf(15f) }
    val highlighterColor = rememberSaveable { mutableLongStateOf(Color.Black.toColorLong()) }
    val highlighterSize = rememberSaveable { mutableFloatStateOf(15f) }

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

    Scaffold { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val rootView = FrameLayout(context)
                    inProgressStrokesView = InProgressStrokesView(context).apply {
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
                        currentBrush = currentBrush
                    )
                    rootView.setOnTouchListener(touchListener)
                }
            )

            Canvas(modifier = Modifier) {
                val canvasTransform = Matrix()
                drawContext.canvas.nativeCanvas.concat(canvasTransform)
                val canvas = drawContext.canvas.nativeCanvas

                finishedStrokesState.value.forEach { stroke ->
                    canvasStrokeRenderer.draw(
                        stroke = stroke, canvas = canvas, strokeToScreenTransform = canvasTransform
                    )
                }
            }

            FloatingToolbar(
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
                            contentDescription = "pressurePen"
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
                            contentDescription = "marker"
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
                            contentDescription = "highlighter"
                        )
                    }
                },
                trailingContent = {
                    ToolbarButton(
                        checked = currentMode == Mode.Eraser,
                        onCheckedChange = { if (it) currentMode = Mode.Eraser },
                        popupContent = {
                            TextButton(
                                onClick = { eraseStrokesInBox(finishedStrokesState) }) {
                                Text("Erase Whole Strokes")
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ink_eraser_24px),
                            contentDescription = "eraser"
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
    currentBrush: Brush
): View.OnTouchListener {
    val predictor = MotionEventPredictor.newInstance(rootView)
    var currentPointerId: Int? = null
    var currentStrokeId: InProgressStrokeId? = null

    return View.OnTouchListener { view, event ->
        when (currentMode) {
            Mode.Brush -> {
                val predictedEvent = predictor.predict()
                try {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.requestUnbufferedDispatch(event)
                            val pointerIndex = event.actionIndex
                            val pointerId = event.getPointerId(pointerIndex)
                            currentPointerId = pointerId
                            currentStrokeId = inProgressStrokesView.startStroke(
                                event = event, pointerId = pointerId, brush = currentBrush
                            )
                            true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val pointerId = checkNotNull(currentPointerId)
                            val strokeId = checkNotNull(currentStrokeId)

                            for (pointerIndex in 0 until event.pointerCount) {
                                if (event.getPointerId(pointerIndex) != pointerId) continue
                                inProgressStrokesView.addToStroke(
                                    event, pointerId, strokeId, predictedEvent
                                )
                            }
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            val pointerIndex = event.actionIndex
                            val pointerId = event.getPointerId(pointerIndex)
//                                        check(pointerId == currentPointerId.value)
                            val currentStrokeId = checkNotNull(currentStrokeId)
                            inProgressStrokesView.finishStroke(
                                event, pointerId, currentStrokeId
                            )
                            view.performClick()
                            true
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            val pointerIndex = event.actionIndex
                            val pointerId = event.getPointerId(pointerIndex)
                            check(pointerId == currentPointerId)
                            val currentStrokeId = checkNotNull(currentStrokeId)
                            inProgressStrokesView.cancelStroke(currentStrokeId, event)
                            true
                        }

                        else -> false
                    }
                } finally {
                    predictedEvent?.recycle()
                }
            }

            Mode.Eraser -> {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        // For stroke-by-stroke eraser, create a small box around the pointer
                        // and erase any intersecting strokes.
                        val eraserSize = 40f // Define the size of the eraser tip
                        val eraserBox = ImmutableBox.fromCenterAndDimensions(
                            ImmutableVec(event.x, event.y), eraserSize, eraserSize
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
    val threshold = 0.1f

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