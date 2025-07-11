package kolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrightnessSlider(
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
    initialColor: Color? = null,
) {
    SideEffect {
        controller.isAttachedBrightnessSlider = true
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
        getValue = { brightness.value },
        setValue = ColorPickerController::setBrightness,
        computeInitial = { maxOf(it.red, it.green, it.blue) },
        getGradientColors = { listOf(Color.Black, pureSelectedColor.value) },
    )
}
