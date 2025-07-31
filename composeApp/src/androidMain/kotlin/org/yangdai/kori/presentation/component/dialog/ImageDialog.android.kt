package org.yangdai.kori.presentation.component.dialog

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection

sealed interface ImageState {
    data object Loading : ImageState
    data class Success(val imagePath: String, val isPath: Boolean, val isLocalFile: Boolean) :
        ImageState

    data class Error(val message: String) : ImageState
    data object Empty : ImageState
}

class ImageViewModel : ViewModel() {
    private val _imageState = MutableStateFlow<ImageState>(ImageState.Empty)
    val imageState = _imageState.asStateFlow()

    fun loadImage(context: Context, imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageState.value = ImageState.Loading
            try {
                val isLocalFile = isLocalFile(imageUrl)
                val imagePath =
                    if (isLocalFile) getLocalFilePath(imageUrl) else downloadAndSaveImage(
                        context,
                        imageUrl
                    )
                _imageState.value =
                    ImageState.Success(imagePath, !imageUrl.startsWith("content://"), isLocalFile)
            } catch (e: Exception) {
                _imageState.value = ImageState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun isLocalFile(url: String): Boolean {
        return url.startsWith("file://") || url.startsWith("content://")
    }

    private fun getLocalFilePath(fileUrl: String): String {
        // 移除 "file:///" 前缀
        return fileUrl.replace("file:///", "")
    }

    private suspend fun downloadAndSaveImage(context: Context, imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            val connection = imageUrl.toHttpUrl().toUrl().openConnection() as HttpURLConnection
            connection.connect()
            // 创建临时文件
            val fileName = "img_${System.currentTimeMillis()}"
            val file = File(context.cacheDir, fileName)

            // 将图片保存到临时文件
            connection.inputStream.use { inputStream ->
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
            }
            file.absolutePath
        }
    }

    // 清理缓存目录中的图片文件
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
actual fun ImageViewerDialog(imageUrl: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val viewModel = viewModel<ImageViewModel>()
    val imageState by viewModel.imageState.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.apply {
                setDimAmount(0.5f)
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it.blurBehindRadius = 16
                    attributes = it
                }
            }
        }
        DisposableEffect(imageUrl) {
            viewModel.loadImage(context.applicationContext, imageUrl)
            onDispose {
                viewModel.clearCache(context.applicationContext)
                activity?.window?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
                dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
            }
        }
        var isHdr by remember { mutableStateOf(false) }
        Box(Modifier.fillMaxSize()) {
            when (val state = imageState) {
                is ImageState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ImageState.Success -> {
                    val bitmap = remember(state.imagePath) {
                        if (state.isPath) BitmapFactory.decodeFile(state.imagePath)
                        else
                            context.contentResolver.openInputStream(state.imagePath.toUri())?.use {
                                BitmapFactory.decodeStream(it)
                            }
                    }?.also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            if (it.hasGainmap()) {
                                isHdr = true
                                activity?.window?.colorMode = ActivityInfo.COLOR_MODE_HDR
                                dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_HDR
                            }
                        }
                    }
                    val imageBitmap = bitmap?.asImageBitmap()
                        ?: run {
                            onDismissRequest()
                            return@Box
                        }
                    ImageViewer(imageBitmap)
                    if (isHdr)
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
                            onClick = {
                                downloadImage(context.applicationContext, imageUrl)
                            }
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
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
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