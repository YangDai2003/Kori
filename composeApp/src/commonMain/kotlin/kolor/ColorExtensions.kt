package kolor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

internal val Color.hexCode: String
    inline get() {
        val a: Int = (alpha * 255).toInt()
        val r: Int = (red * 255).toInt()
        val g: Int = (green * 255).toInt()
        val b: Int = (blue * 255).toInt()
        return a.hex + r.hex + g.hex + b.hex
    }

private val Int.hex get() = this.toString(16).padStart(2, '0')

internal fun Color.toHSV(): Triple<Float, Float, Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val diff = max - min

    val h = if (diff == 0f) {
        0.0f
    } else if (max == red) {
        (60 * ((green - blue) / diff) + 360f) % 360f
    } else if (max == green) {
        (60 * ((blue - red) / diff) + 120f) % 360f
    } else {
        (60 * ((red - green) / diff) + 240f) % 360f
    }
    val s = if (max == 0f) 0f else diff / max

    return Triple(h, s, max)
}

/** Converts an HS(V) color to a coordinate on the hue/saturation circle. */
internal fun hsvToCoord(h: Float, s: Float, center: Offset) =
    Offset.fromAngle(hueToAngle(h), s * center.minCoordinate) + center

internal fun angleToHue(angle: Float) = (-angle.toDegrees() + 360f) % 360f
internal fun hueToAngle(hue: Float) = -hue.toRadians()
