package org.yangdai.kori.presentation.component.dialog

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed interface ImageState {
    data object Loading : ImageState
    data class Success(
        val imageBitmap: ImageBitmap,
        val isHdr: Boolean,
        val isLocalFile: Boolean
    ) : ImageState

    data class Error(val message: String) : ImageState
    data object Empty : ImageState
}

class ImageViewModel : ViewModel() {
    private val _imageState = MutableStateFlow<ImageState>(ImageState.Empty)
    val imageState = _imageState.asStateFlow()

    fun loadImage(context: Context, imageUrl: String, reqWidth: Int, reqHeight: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageState.value = ImageState.Loading
            try {
                val isLocalFile = imageUrl.contains(WebViewAssetLoader.DEFAULT_DOMAIN)
                val imagePath = if (isLocalFile) {
                    context.filesDir.absolutePath + imageUrl.removePrefix("https://${WebViewAssetLoader.DEFAULT_DOMAIN}/files")
                } else {
                    downloadAndSaveImage(context, imageUrl)
                }

                val bitmap = decodeSampledBitmapFromFile(imagePath, reqWidth, reqHeight)
                if (bitmap != null) {
                    var isHdr = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (bitmap.hasGainmap()) {
                            isHdr = true
                        }
                    }
                    _imageState.value =
                        ImageState.Success(bitmap.asImageBitmap(), isHdr, isLocalFile)
                } else {
                    _imageState.value = ImageState.Error("Failed to decode image")
                }
            } catch (e: Exception) {
                _imageState.value = ImageState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun decodeSampledBitmapFromFile(
        imagePath: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
            BitmapFactory.decodeFile(imagePath, this)
        }
    }

    private suspend fun downloadAndSaveImage(context: Context, imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            connection.connect()
            val fileName = "img_${System.currentTimeMillis()}"
            val file = File(context.cacheDir, fileName)

            connection.inputStream.use { inputStream ->
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
            }
            file.absolutePath
        }
    }

    fun clearCache(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageState.value = ImageState.Empty
            try {
                context.cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("img_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageViewerDialog(imageUrl: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val activity = LocalActivity.current
    val viewModel = viewModel<ImageViewModel>()
    val imageState by viewModel.imageState.collectAsStateWithLifecycle()
    val size = LocalWindowInfo.current.containerSize

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it.blurBehindRadius = 20
                    attributes = it
                }
            }
        }
        DisposableEffect(imageUrl) {
            viewModel.loadImage(context, imageUrl, size.width, size.height)
            onDispose {
                viewModel.clearCache(context)
                activity?.window?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
                dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
            }
        }
        Box(Modifier.fillMaxSize()) {
            when (val state = imageState) {
                is ImageState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ImageState.Success -> {
                    SideEffect {
                        if (state.isHdr && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            activity?.window?.colorMode = ActivityInfo.COLOR_MODE_HDR
                            dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_HDR
                        }
                    }
                    ImageViewer(state.imageBitmap)
                    if (state.isHdr)
                        Icon(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp),
                            imageVector = Icons.Default.HdrOn,
                            tint = Color.White,
                            contentDescription = "HDR"
                        )
                    if (!state.isLocalFile)
                        FilledTonalIconButton(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 16.dp),
                            onClick = { downloadImage(context, imageUrl) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Image"
                            )
                        }
                }

                is ImageState.Error -> onDismissRequest()

                ImageState.Empty -> {}
            }
            FilledTonalIconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
private fun ImageViewer(imageBitmap: ImageBitmap) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }
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
        modifier = Modifier
            .fillMaxSize()
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

private fun downloadImage(context: Context, url: String) {
    try {
        val request = DownloadManager.Request(url.toUri()).setTitle("🖼️")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "Image_${System.currentTimeMillis()}_OpenNote"
            ).setAllowedOverMetered(true).setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}