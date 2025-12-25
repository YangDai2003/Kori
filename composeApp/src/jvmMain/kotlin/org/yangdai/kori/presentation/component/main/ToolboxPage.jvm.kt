package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kfile.PlatformFile
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.drag_drop
import org.jetbrains.compose.resources.stringResource
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
actual fun WidgetListItem() {
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun DropTarget(onFilePicked: (PlatformFile) -> Unit) {
    var animBorder by remember { mutableStateOf(false) }
    var fileAttached by remember { mutableStateOf(false) }
    val target = remember {
        object : DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                animBorder = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                animBorder = false
            }

            override fun onEntered(event: DragAndDropEvent) {
                fileAttached = true
            }

            override fun onExited(event: DragAndDropEvent) {
                fileAttached = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                // Prints the type of action into system output every time
                // a drag-and-drop operation is concluded.
                println("Action at the target: ${event.action}")

                event.awtTransferable.let { transferable ->
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        try {
                            val fileList =
                                transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                            val firstFile = fileList?.firstOrNull() as? File
                            if (firstFile != null) onFilePicked(PlatformFile(firstFile))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                return true
            }
        }
    }

    val animatedPhase by animateFloatAsState(
        targetValue = if (animBorder) 30f else 0f, // 30f 是一个合适的相位偏移值
        animationSpec = if (animBorder) {
            infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "phaseAnimation"
    )
    val borderColor = MaterialTheme.colorScheme.outline
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16.0f / 9.0f)
            .background(MaterialTheme.colorScheme.surfaceVariant, CardDefaults.shape)
            .padding(8.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(20f, 10f), // 20px dash, 10px gap
                            phase = animatedPhase
                        )
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .dragAndDropTarget(
                // With "true" as the value of shouldStartDragAndDrop,
                // drag-and-drop operations are enabled unconditionally.
                shouldStartDragAndDrop = { true },
                target = target
            ),
        contentAlignment = Alignment.Center
    ) {
        if (fileAttached)
            Icon(
                imageVector = Icons.Default.FilePresent,
                contentDescription = null
            )
        else
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(Res.string.drag_drop),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "MARKDOWN, HTML, TXT",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
    }
}