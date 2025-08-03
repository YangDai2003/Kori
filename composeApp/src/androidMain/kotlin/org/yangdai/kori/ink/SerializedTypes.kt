package org.yangdai.kori.ink

import kotlinx.serialization.Serializable

@Serializable
enum class Mode {
    Brush,
    Eraser,
    View
}

@Serializable
data class SerializedStroke(
    val inputs: SerializedStrokeInputBatch,
    val brush: SerializedBrush
)

@Serializable
data class SerializedBrush(
    val size: Float,
    val color: Long,
    val epsilon: Float,
    val stockBrush: SerializedStockBrush
)

@Serializable
enum class SerializedStockBrush {
    MARKER,
    PRESSURE_PEN,
    HIGHLIGHTER
}

@Serializable
data class SerializedStrokeInputBatch(
    val toolType: SerializedToolType,
    val strokeUnitLengthCm: Float,
    val inputs: List<SerializedStrokeInput>
)

@Serializable
data class SerializedStrokeInput(
    val x: Float,
    val y: Float,
    val timeMillis: Float,
    val pressure: Float,
    val tiltRadians: Float,
    val orientationRadians: Float,
    val strokeUnitLengthCm: Float
)

@Serializable
enum class SerializedToolType {
    STYLUS,
    TOUCH,
    MOUSE,
    UNKNOWN
}