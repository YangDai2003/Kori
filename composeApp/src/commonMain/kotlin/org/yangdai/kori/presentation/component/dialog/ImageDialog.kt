package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

@Composable
fun ImageViewer(imageBitmap: ImageBitmap) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0f) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        val adjustedOffset = offsetChange * scale
        val maxX = (imageSize.width * scale - containerSize.width) / 2
        val maxY = (imageSize.height * scale - containerSize.height) / 2
        offset = Offset(
            x = (offset.x + adjustedOffset.x).coerceIn(-maxX, maxX),
            y = (offset.y + adjustedOffset.y).coerceIn(-maxY, maxY)
        )
        rotation += rotationChange
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
            .onSizeChanged { containerSize = it }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
                rotationZ = rotation
            }
            .transformable(state)
            .onSizeChanged { imageSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { // 长按复位
                        scale = 1f
                        offset = Offset.Zero
                        rotation = 0f
                    },
                    onDoubleTap = { tapOffset -> // 双击切换缩放
                        val targetScale = if (scale > 1f) 1f else 2f
                        val containerCenter =
                            Offset(containerSize.width / 2f, containerSize.height / 2f)
                        val tapOffsetFromCenter = (tapOffset - containerCenter - offset) / scale
                        val targetPointInContainer =
                            tapOffsetFromCenter * targetScale + containerCenter
                        val targetOffset = tapOffset - targetPointInContainer
                        val maxX = (imageSize.width * targetScale - containerSize.width) / 2
                        val maxY = (imageSize.height * targetScale - containerSize.height) / 2
                        val newOffset = if (targetScale > 1f) {
                            Offset(
                                x = targetOffset.x.coerceIn(-maxX, maxX),
                                y = targetOffset.y.coerceIn(-maxY, maxY)
                            )
                        } else {
                            Offset.Zero
                        }
                        scale = targetScale
                        offset = newOffset
                    }
                )
            }
    )
}

@Composable
expect fun ImageViewerDialog(imageUrl: String, onDismissRequest: () -> Unit)