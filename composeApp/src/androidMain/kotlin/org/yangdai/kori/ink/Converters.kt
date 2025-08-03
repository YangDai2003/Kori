package org.yangdai.kori.ink

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.ink.brush.Brush
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.InputToolType
import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.Stroke
import androidx.ink.strokes.StrokeInput
import androidx.ink.strokes.StrokeInputBatch
import kotlinx.serialization.json.Json

class Converters {

    companion object {
        val stockBrushToEnumValues =
            mapOf(
                StockBrushes.markerLatest to SerializedStockBrush.MARKER,
                StockBrushes.pressurePenLatest to SerializedStockBrush.PRESSURE_PEN,
                StockBrushes.highlighterLatest to SerializedStockBrush.HIGHLIGHTER,
            )

        val enumToStockBrush =
            stockBrushToEnumValues.entries.associate { (key, value) -> value to key }
    }

    private fun serializeBrush(brush: Brush): SerializedBrush {
        return SerializedBrush(
            size = brush.size,
            color = brush.colorLong,
            epsilon = brush.epsilon,
            stockBrush = stockBrushToEnumValues[brush.family] ?: SerializedStockBrush.PRESSURE_PEN,
        )
    }

    private fun serializeStrokeInputBatch(inputs: StrokeInputBatch): SerializedStrokeInputBatch {
        val serializedInputs = mutableListOf<SerializedStrokeInput>()
        val scratchInput = StrokeInput()

        for (i in 0 until inputs.size) {
            inputs.populate(i, scratchInput)
            serializedInputs.add(
                SerializedStrokeInput(
                    x = scratchInput.x,
                    y = scratchInput.y,
                    timeMillis = scratchInput.elapsedTimeMillis.toFloat(),
                    pressure = scratchInput.pressure,
                    tiltRadians = scratchInput.tiltRadians,
                    orientationRadians = scratchInput.orientationRadians,
                    strokeUnitLengthCm = scratchInput.strokeUnitLengthCm,
                )
            )
        }

        val toolType =
            when (inputs.getToolType()) {
                InputToolType.STYLUS -> SerializedToolType.STYLUS
                InputToolType.TOUCH -> SerializedToolType.TOUCH
                InputToolType.MOUSE -> SerializedToolType.MOUSE
                else -> SerializedToolType.UNKNOWN
            }

        return SerializedStrokeInputBatch(
            toolType = toolType,
            strokeUnitLengthCm = inputs.getStrokeUnitLengthCm(),
            inputs = serializedInputs,
        )
    }

    private fun deserializeStroke(serializedStroke: SerializedStroke): Stroke {
        val inputs = deserializeStrokeInputBatch(serializedStroke.inputs)
        val brush = deserializeBrush(serializedStroke.brush)
        return Stroke(brush = brush, inputs = inputs)
    }

    private fun deserializeBrush(serializedBrush: SerializedBrush): Brush {
        val stockBrushFamily =
            enumToStockBrush[serializedBrush.stockBrush] ?: StockBrushes.pressurePenLatest

        return Brush.createWithColorLong(
            family = stockBrushFamily,
            colorLong = serializedBrush.color,
            size = serializedBrush.size,
            epsilon = serializedBrush.epsilon,
        )
    }

    private fun deserializeStrokeInputBatch(
        serializedBatch: SerializedStrokeInputBatch
    ): StrokeInputBatch {
        val toolType =
            when (serializedBatch.toolType) {
                SerializedToolType.STYLUS -> InputToolType.STYLUS
                SerializedToolType.TOUCH -> InputToolType.TOUCH
                SerializedToolType.MOUSE -> InputToolType.MOUSE
                else -> InputToolType.UNKNOWN
            }

        val batch = MutableStrokeInputBatch()

        serializedBatch.inputs.forEach { input ->
            batch.add(
                type = toolType,
                x = input.x,
                y = input.y,
                elapsedTimeMillis = input.timeMillis.toLong(),
                pressure = input.pressure,
                tiltRadians = input.tiltRadians,
                orientationRadians = input.orientationRadians,
            )
        }

        return batch
    }

    fun serializeStrokeToString(stroke: Stroke): Pair<String, String> {
        val serializedBrush = serializeBrush(stroke.brush)
        val serializedInputs = serializeStrokeInputBatch(stroke.inputs)
        return Pair(
            first = Json.encodeToString(serializedBrush),
            second = Json.encodeToString(serializedInputs),
        )
    }

    fun deserializeStringToStroke(pair: Pair<String, String>): Stroke {
        val serializedBrush = pair.first.let {
            Json.decodeFromString<SerializedBrush>(it)
        }
        val serializedInputs = pair.second.let {
            Json.decodeFromString<SerializedStrokeInputBatch>(it)
        }

        val serializedStroke = SerializedStroke(serializedInputs, serializedBrush)
        return deserializeStroke(serializedStroke)
    }
}

fun brushFamilySaver(): Saver<MutableState<BrushFamily>, Int> = Saver(
    save = { state ->
        val serializedStockBrush =
            Converters.stockBrushToEnumValues[state.value] ?: SerializedStockBrush.PRESSURE_PEN
        serializedStockBrush.ordinal
    },
    restore = { ordinal ->
        val serializedStockBrush = SerializedStockBrush.entries.find { it.ordinal == ordinal }
            ?: SerializedStockBrush.PRESSURE_PEN
        val stockBrush =
            Converters.enumToStockBrush[serializedStockBrush] ?: StockBrushes.pressurePenLatest
        mutableStateOf(stockBrush)
    }
)