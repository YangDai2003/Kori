package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt

@Composable
fun VerticalDragHandle(
    modifier: Modifier = Modifier,
    colors: ColorScheme = MaterialTheme.colorScheme,
    shapes: Shapes = MaterialTheme.shapes,
    interactionSource: MutableInteractionSource,
) {
    val isDragged by interactionSource.collectIsDraggedAsState()
    var isPressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .hoverable(interactionSource)
            .pressable(interactionSource, { isPressed = true }, { isPressed = false })
            .graphicsLayer {
                shape =
                    when {
                        isDragged -> shapes.medium
                        isPressed -> shapes.medium
                        else -> shapes.large
                    }
                clip = true
            }
            .layout { measurable, _ ->
                val dragHandleSize =
                    when {
                        isDragged -> DpSize(12.dp, 52.dp)
                        isPressed -> DpSize(12.dp, 52.dp)
                        else -> DpSize(4.dp, 48.dp)
                    }.toSize()
                // set constraints here to be the size needed
                val placeable =
                    measurable.measure(
                        Constraints.fixed(
                            dragHandleSize.width.fastRoundToInt(),
                            dragHandleSize.height.fastRoundToInt()
                        )
                    )
                layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
            }
            .drawBehind {
                drawRect(
                    when {
                        isDragged -> colors.onSurface
                        isPressed -> colors.onSurface
                        else -> colors.outline
                    }
                )
            }
            .indication(interactionSource, ripple())
    )
}

private fun Modifier.pressable(
    interactionSource: MutableInteractionSource,
    onPressed: () -> Unit,
    onReleasedOrCancelled: () -> Unit
): Modifier =
    pointerInput(interactionSource) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            onPressed()
            waitForUpOrCancellation(pass = PointerEventPass.Initial)
            onReleasedOrCancelled()
        }
    }
