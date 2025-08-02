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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ink.authoring.InProgressStrokeId
import androidx.ink.authoring.InProgressStrokesFinishedListener
import androidx.ink.authoring.InProgressStrokesView
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.ink.geometry.ImmutableBox
import androidx.ink.geometry.Vec
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import androidx.input.motionprediction.MotionEventPredictor

@SuppressLint("ClickableViewAccessibility")
@Composable
fun DrawingView() {
    val canvasStrokeRenderer = remember { CanvasStrokeRenderer.create() }
    var currentPointerId by remember { mutableStateOf<Int?>(null) }
    var currentStrokeId by remember { mutableStateOf<InProgressStrokeId?>(null) }
    val finishedStrokesState = remember { mutableStateOf(setOf<Stroke>()) }
    val defaultBrush = Brush.createWithColorIntArgb(
        family = StockBrushes.pressurePenLatest,
        colorIntArgb = Color.Black.toArgb(),
        size = 5F,
        epsilon = 0.1F
    )

    Scaffold { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val rootView = FrameLayout(context)
                    val inProgressStrokesView = InProgressStrokesView(context).apply {
                        eagerInit()
                        addFinishedStrokesListener(
                            object : InProgressStrokesFinishedListener {
                                override fun onStrokesFinished(strokes: Map<InProgressStrokeId, Stroke>) {
                                    finishedStrokesState.value += strokes.values
                                    removeFinishedStrokes(strokes.keys)
                                }
                            }
                        )
                    }
                    val predictor = MotionEventPredictor.newInstance(rootView)
                    val touchListener =
                        View.OnTouchListener { view, event ->
                            predictor.record(event)
                            val predictedEvent = predictor.predict()

                            try {
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN -> {
                                        // First pointer - treat it as inking.
                                        view.requestUnbufferedDispatch(event)
                                        val pointerIndex = event.actionIndex
                                        val pointerId = event.getPointerId(pointerIndex)
                                        currentPointerId = pointerId
                                        currentStrokeId =
                                            inProgressStrokesView.startStroke(
                                                event = event,
                                                pointerId = pointerId,
                                                brush = defaultBrush
                                            )
                                        true
                                    }

                                    MotionEvent.ACTION_MOVE -> {
                                        val pointerId = checkNotNull(currentPointerId)
                                        val strokeId = checkNotNull(currentStrokeId)

                                        for (pointerIndex in 0 until event.pointerCount) {
                                            if (event.getPointerId(pointerIndex) != pointerId) continue
                                            inProgressStrokesView.addToStroke(
                                                event,
                                                pointerId,
                                                strokeId,
                                                predictedEvent
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
                                            event,
                                            pointerId,
                                            currentStrokeId
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
                    rootView.setOnTouchListener(touchListener)
                    rootView.addView(inProgressStrokesView)
                    rootView
                }
            )

            Canvas(modifier = Modifier) {
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

            FilledIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onClick = {
                    eraseWholeStrokes(
                        finishedStrokesState = finishedStrokesState
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear"
                )
            }
        }
    }
}

fun eraseWholeStrokes(
    eraserBox: ImmutableBox = ImmutableBox.fromCenterAndDimensions(
        Vec.ORIGIN, Float.MAX_VALUE, Float.MAX_VALUE
    ),
    finishedStrokesState: MutableState<Set<Stroke>>,
) {
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