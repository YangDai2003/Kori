package org.yangdai.kori.presentation.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Preview
@Composable
private fun SegmentedDemo() {
    MaterialTheme {
        Surface {
            Column(Modifier.padding(8.dp), verticalArrangement = spacedBy(16.dp)) {
                Text("SEGMENTS")
                val twoSegments = remember { listOf("Foo", "Bar") }
                var selectedSegmentIndex by remember { mutableIntStateOf(0) }
                SegmentedControl(
                    twoSegments,
                    selectedSegmentIndex,
                    onSegmentSelected = { selectedSegmentIndex = it }
                ) {
                    SegmentText(it)
                }

                val threeSegments = remember { listOf("Foo", "Bar", "Some very long string") }
                var selectedIndex by remember { mutableIntStateOf(0) }
                SegmentedControl(
                    threeSegments,
                    selectedIndex,
                    onSegmentSelected = { selectedIndex = it }
                ) {
                    SegmentText(it)
                }
            }
        }
    }
}

private const val NO_SEGMENT_INDEX = -1
private val TRACK_PADDING = 2.dp
private val PRESSED_TRACK_PADDING = 1.dp
private val SEGMENT_PADDING = 5.dp
private const val PRESSED_UNSELECTED_ALPHA = .6f
private val BACKGROUND_SHAPE = RoundedCornerShape(8.dp)
private val THUMB_SHAPE = RoundedCornerShape(7.dp)

/**
 * Creates and remembers a [SegmentedControlState] instance.
 */
@Composable
private fun rememberSegmentedControlState(
    segmentCount: Int,
    selectedIndex: Int,
    onSegmentSelected: (index: Int) -> Unit
): SegmentedControlState {
    return remember {
        SegmentedControlState()
    }.apply {
        this.segmentCount = segmentCount
        this.selectedIndex = selectedIndex
        this.onSegmentSelected = onSegmentSelected
    }
}


/**
 * A state holder for the [SegmentedControl] composable. This class is now free of @Composable invocations.
 */
@Stable
private class SegmentedControlState {
    var segmentCount by mutableStateOf(0)
    var selectedIndex by mutableStateOf(0)
    lateinit var onSegmentSelected: (index: Int) -> Unit
    var pressedSegment by mutableStateOf(NO_SEGMENT_INDEX)
    var pressedSelectedScale by mutableStateOf(1f)

    fun updatePressedScale(controlHeight: Int, density: Density) {
        with(density) {
            val pressedPadding = PRESSED_TRACK_PADDING * 2
            val pressedHeight = controlHeight - pressedPadding.toPx()
            pressedSelectedScale = pressedHeight / controlHeight
        }
    }
}

@Composable
fun <T : Any> SegmentedControl(
    segments: List<T>,
    selectedSegmentIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val state = rememberSegmentedControlState(
        segmentCount = segments.size,
        selectedIndex = selectedSegmentIndex,
        onSegmentSelected = { index -> onSegmentSelected(index) }
    )

    val selectedIndexOffset by animateFloatAsState(targetValue = state.selectedIndex.toFloat())

    val inputModifier = Modifier.pointerInput(state.segmentCount) {
        val segmentWidth = size.width / state.segmentCount

        fun segmentIndex(change: PointerInputChange): Int =
            ((change.position.x / size.width.toFloat()) * state.segmentCount)
                .toInt()
                .coerceIn(0, state.segmentCount - 1)

        awaitEachGesture {
            val down = awaitFirstDown()
            state.pressedSegment = segmentIndex(down)
            val downOnSelected = state.pressedSegment == state.selectedIndex
            val segmentBounds = Rect(
                left = state.pressedSegment * segmentWidth.toFloat(),
                right = (state.pressedSegment + 1) * segmentWidth.toFloat(),
                top = 0f,
                bottom = size.height.toFloat()
            )

            if (downOnSelected) {
                horizontalDrag(down.id) { change ->
                    if (abs(change.position.y - change.previousPosition.y) > 0) {
                        change.consume()
                    }

                    state.pressedSegment = segmentIndex(change)
                    if (state.pressedSegment != state.selectedIndex) {
                        state.onSegmentSelected(state.pressedSegment)
                    }
                }
            } else {
                waitForUpOrCancellation(inBounds = segmentBounds)
                    ?.let { state.onSegmentSelected(state.pressedSegment) }
            }
            state.pressedSegment = NO_SEGMENT_INDEX
        }
    }

    Layout(
        content = {
            Thumb(state)
            Dividers(state)
            Segments(state, segments, content)
        },
        modifier = modifier
            .fillMaxWidth()
            .then(inputModifier)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, BACKGROUND_SHAPE)
            .padding(TRACK_PADDING)
    ) { measurables, constraints ->
        val (thumbMeasurable, dividersMeasurable, segmentsMeasurable) = measurables

        val segmentsPlaceable = segmentsMeasurable.measure(constraints)
        // Side-effect: Update the scale factor based on the measured height.
        // This will cause a recomposition but is a pragmatic way to solve the dependency.
        state.updatePressedScale(segmentsPlaceable.height, this)

        val segmentWidth = segmentsPlaceable.width / segments.size

        val thumbPlaceable = thumbMeasurable.measure(
            Constraints.fixed(width = segmentWidth, height = segmentsPlaceable.height)
        )
        val dividersPlaceable = dividersMeasurable.measure(
            Constraints.fixed(width = segmentsPlaceable.width, height = segmentsPlaceable.height)
        )

        layout(segmentsPlaceable.width, segmentsPlaceable.height) {
            thumbPlaceable.placeRelative(
                x = (selectedIndexOffset * segmentWidth).toInt(),
                y = 0
            )
            dividersPlaceable.placeRelative(IntOffset.Zero)
            segmentsPlaceable.placeRelative(IntOffset.Zero)
        }
    }
}

@Composable
fun SegmentText(text: String) =
    Text(
        modifier = Modifier.basicMarquee(),
        text = text,
        maxLines = 1,
        style = MaterialTheme.typography.labelMedium.copy(textMotion = TextMotion.Animated)
    )

@Composable
private fun Thumb(state: SegmentedControlState) =
    Box(
        Modifier
            .segmentScaleModifier(
                pressed = state.pressedSegment == state.selectedIndex,
                pressedSelectedScale = state.pressedSelectedScale,
                segment = state.selectedIndex,
                segmentCount = state.segmentCount,
            )
            .shadow(1.dp, THUMB_SHAPE)
            .background(MaterialTheme.colorScheme.surface, THUMB_SHAPE)
    )

@Composable
private fun Dividers(state: SegmentedControlState) {
    val color = MaterialTheme.colorScheme.outline
    val alphas = (0 until state.segmentCount).map { i ->
        val selectionAdjacent = i == state.selectedIndex || i - 1 == state.selectedIndex
        animateFloatAsState(
            targetValue = if (selectionAdjacent) 0f else 1f,
            label = "dividerAlpha$i"
        )
    }
    Canvas(Modifier.fillMaxSize()) {
        if (state.segmentCount <= 1) return@Canvas
        val segmentWidth = size.width / state.segmentCount
        val dividerPadding = TRACK_PADDING + PRESSED_TRACK_PADDING

        alphas.forEachIndexed { i, alpha ->
            // 跳过第一个（索引为0），因为它没有左侧分割线
            if (i == 0) return@forEachIndexed

            val currentAlpha = alpha.value // 读取动画的当前值
            if (currentAlpha > 0f) {
                val x = i * segmentWidth
                drawLine(
                    color = color,
                    alpha = currentAlpha,
                    start = Offset(x, dividerPadding.toPx()),
                    end = Offset(x, size.height - dividerPadding.toPx())
                )
            }
        }
    }
}

@Composable
private fun <T> Segments(
    state: SegmentedControlState,
    segments: List<T>,
    content: @Composable (T) -> Unit
) = Row(
    horizontalArrangement = spacedBy(TRACK_PADDING),
    modifier = Modifier
        .fillMaxWidth()
        .selectableGroup()
) {
    segments.forEachIndexed { i, segment ->
        val isSelected = i == state.selectedIndex
        val isPressed = i == state.pressedSegment
        val alpha by animateFloatAsState(if (!isSelected && isPressed) PRESSED_UNSELECTED_ALPHA else 1f)

        val semanticsModifier = Modifier.semantics(mergeDescendants = true) {
            this.selected = isSelected
            this.role = Role.Button
            onClick { state.onSegmentSelected(i); true }
            stateDescription = if (isSelected) "Selected" else "Not selected"
        }

        Box(
            Modifier
                .weight(1f)
                .then(semanticsModifier)
                .padding(SEGMENT_PADDING)
                .graphicsLayer { this.alpha = alpha }
                .segmentScaleModifier(
                    pressed = isPressed && isSelected,
                    pressedSelectedScale = state.pressedSelectedScale,
                    segment = i,
                    segmentCount = state.segmentCount
                )
                .wrapContentWidth()
        ) {
            content(segment)
        }
    }
}

/**
 * A modifier that scales an element when pressed.
 * It uses `Modifier.composed` to gain a @Composable context for animations.
 */
private fun Modifier.segmentScaleModifier(
    pressed: Boolean,
    pressedSelectedScale: Float,
    segment: Int,
    segmentCount: Int
): Modifier = this.composed {
    val scale by animateFloatAsState(if (pressed) pressedSelectedScale else 1f)
    val xOffset by animateDpAsState(if (pressed) PRESSED_TRACK_PADDING else 0.dp)

    graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale

        if (segmentCount > 1) {
            this.transformOrigin = TransformOrigin(
                pivotFractionX = when (segment) {
                    0 -> 0f
                    segmentCount - 1 -> 1f
                    else -> .5f
                },
                pivotFractionY = .5f
            )

            this.translationX = when (segment) {
                0 -> xOffset.toPx()
                segmentCount - 1 -> -xOffset.toPx()
                else -> 0f
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.waitForUpOrCancellation(inBounds: Rect): PointerInputChange? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.all { it.changedToUp() }) {
            return event.changes[0]
        }

        if (event.changes.any { it.isConsumed || !inBounds.contains(it.position) }) {
            return null
        }

        val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
        if (consumeCheck.changes.any { it.isConsumed }) {
            return null
        }
    }
}