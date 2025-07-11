package kolor

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradient
import androidx.compose.ui.graphics.TileMode

/**
 * Draws hsv color gradient with hue and saturation on a canvas.
 */
internal fun Canvas.drawHsvColorGradient(size: Size) {
    val center = size.center
    val radius = size.minDimension * 0.5f
    hsvSweepGradient.applyTo(size, huePaint, 1f)
    saturationGradient.applyTo(size, saturationPaint, 1f)
    drawCircle(center, radius, huePaint)
    drawCircle(center, radius, saturationPaint)
}

private val huePaint: Paint = Paint().apply { isAntiAlias = true }
private val saturationPaint: Paint = Paint().apply { isAntiAlias = true }

private val hsvSweepGradient = Brush.sweepGradient(
    0.000f to Color.Red,
    0.166f to Color.Magenta,
    0.333f to Color.Blue,
    0.499f to Color.Cyan,
    0.666f to Color.Green,
    0.833f to Color.Yellow,
    0.999f to Color.Red,
    // center = center,
)

private val saturationGradient = Brush.radialGradient(
    0f to Color(0xFFFFFFFF),
    1f to Color(0x00FFFFFF),
    // center = center,
    // radius = radius,
    tileMode = TileMode.Clamp,
) as RadialGradient
