package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.insert_table
import org.jetbrains.compose.resources.stringResource

@Preview
@Composable
private fun TableDialogPreview() {
    TableDialog(
        onConfirm = { rows, cols ->
            println("Selected rows: $rows, columns: $cols")
        },
        onDismissRequest = {}
    )
}

/**
 * A dialog that allows users to select table dimensions by dragging over a grid.
 *
 * @param onConfirm Callback invoked when user confirms the table creation with selected dimensions.
 * @param onDismissRequest Callback invoked when the dialog is dismissed.
 */
@Composable
fun TableDialog(
    onConfirm: (rows: Int, columns: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedRows by remember { mutableStateOf(3) }
    var selectedColumns by remember { mutableStateOf(3) }

    AlertDialog(
        modifier = Modifier.widthIn(max = DialogMaxWidth),
        onDismissRequest = onDismissRequest,
        confirmButton = { ConfirmButton { onConfirm(selectedRows, selectedColumns) } },
        dismissButton = { DismissButton(onDismissRequest) },
        title = { Text(stringResource(Res.string.insert_table)) },
        shape = dialogShape(),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$selectedRows × $selectedColumns",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TableGridSelector { rows, cols ->
                    selectedRows = rows
                    selectedColumns = cols
                }
            }
        }
    )
}

@Composable
private fun TableGridSelector(
    initialGridSize: Int = 3,
    maxGridSize: Int = 9,
    cellSpacing: Dp = 2.dp,
    cellSize: Dp = 20.dp,
    onSelectionChange: (rows: Int, columns: Int) -> Unit
) {
    var currentRows by remember { mutableStateOf(initialGridSize) }
    var currentCols by remember { mutableStateOf(initialGridSize) }

    val density = LocalDensity.current
    val cellSpacingPx = with(density) { cellSpacing.toPx() }
    val cellSizePx = with(density) { cellSize.toPx() }
    val width = cellSize * maxGridSize + cellSpacing * (maxGridSize + 1)

    val selectedColor = MaterialTheme.colorScheme.secondary
    val unselectedColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = Modifier.size(width)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val offset = change.position
                    val row = ((offset.y) / (cellSizePx + cellSpacingPx)).toInt() + 1
                    val col = ((offset.x) / (cellSizePx + cellSpacingPx)).toInt() + 1
                    if (row in 1..maxGridSize && col in 1..maxGridSize) {
                        currentRows = row.fastCoerceIn(3, maxGridSize)
                        currentCols = col.fastCoerceIn(2, maxGridSize)
                        onSelectionChange(currentRows, currentCols)
                    }
                }
            }
    ) {
        val totalCellSpace = cellSizePx + cellSpacingPx
        for (row in 0 until maxGridSize) {
            for (col in 0 until maxGridSize) {
                val isSelected = (row + 1) <= currentRows && (col + 1) <= currentCols
                val cellColor = if (isSelected) selectedColor else unselectedColor
                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(
                        x = col * totalCellSpace + cellSpacingPx,
                        y = row * totalCellSpace + cellSpacingPx
                    ),
                    cornerRadius = CornerRadius(4f),
                    size = Size(cellSizePx, cellSizePx)
                )
            }
        }
    }
}
