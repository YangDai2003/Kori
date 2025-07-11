package kolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val defaultTileOddColor: Color = Color(0xFFFFFFFF)
private val defaultTileEvenColor: Color = Color(0xFFCBCBCB)

@Composable
fun AlphaSlider(
    modifier: Modifier = Modifier,
    controller: ColorPickerController,
    borderRadius: Dp = 6.dp,
    borderSize: Dp = 5.dp,
    borderColor: Color = Color.LightGray,
    wheelImageBitmap: ImageBitmap? = null,
    wheelRadius: Dp = 12.dp,
    wheelColor: Color = Color.White,
    wheelAlpha: Float = 1.0f,
    wheelPaint: Paint = Paint().apply {
        color = wheelColor
        alpha = wheelAlpha
    },
    tileOddColor: Color = defaultTileOddColor,
    tileEvenColor: Color = defaultTileEvenColor,
    tileSize: Dp = 12.dp,
    initialColor: Color? = null,
) {
    val density = LocalDensity.current
    val paint = alphaTilePaint(
        with(density) { tileSize.toPx() },
        tileOddColor,
        tileEvenColor,
    )

    SideEffect {
        controller.isAttachedAlphaSlider = true
    }

    Slider(
        modifier = modifier,
        controller = controller,
        borderRadius = borderRadius,
        borderSize = borderSize,
        borderColor = borderColor,
        wheelImageBitmap = wheelImageBitmap,
        wheelRadius = wheelRadius,
        wheelColor = wheelColor,
        wheelAlpha = wheelAlpha,
        wheelPaint = wheelPaint,
        initialColor = initialColor,
        drawBackground = {
            drawRoundRect(it, borderRadius.value, paint)
        },
        getValue = { alpha.value },
        setValue = ColorPickerController::setAlpha,
        computeInitial = { it.alpha },
        getGradientColors = {
            val color = pureSelectedColor.value
            listOf(color.copy(alpha = 0f), color.copy(alpha = 1f))
        },
    )
}
