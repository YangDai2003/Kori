package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.posix.memcpy

sealed interface ImageState {
    data object Loading : ImageState
    data class Success(val imagePath: String, val isLocalFile: Boolean) : ImageState
    data class Error(val message: String) : ImageState
    data object Empty : ImageState
}

class ImageViewModel : ViewModel() {
    private val _imageState = MutableStateFlow<ImageState>(ImageState.Empty)
    val imageState = _imageState.asStateFlow()

    fun loadImage(imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageState.value = ImageState.Loading
            try {
                val isLocalFile = isLocalFile(imageUrl)
                val imagePath =
                    if (isLocalFile) getLocalFilePath(imageUrl) else downloadAndSaveImage(imageUrl)
                _imageState.value = ImageState.Success(imagePath, isLocalFile)
            } catch (e: Exception) {
                _imageState.value = ImageState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun isLocalFile(url: String): Boolean {
        return url.startsWith("file://")
    }

    private fun getLocalFilePath(fileUrl: String): String {
        return fileUrl.removePrefix("file://")
    }

    private suspend fun downloadAndSaveImage(imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            val tmpDir = NSTemporaryDirectory()
            val fileName = "img_${NSDate().timeIntervalSince1970}"
            val filePath = tmpDir + fileName
            val url = NSURL.URLWithString(imageUrl) ?: throw Exception("Invalid URL")
            val data = NSData.dataWithContentsOfURL(url) ?: throw Exception("Download failed")
            if (!data.writeToFile(filePath, true)) throw Exception("Save failed")
            filePath
        }
    }
}

@Composable
actual fun ImageViewerDialog(imageUrl: String, onDismissRequest: () -> Unit) {
    val viewModel = viewModel<ImageViewModel>()
    val imageState by viewModel.imageState.collectAsStateWithLifecycle()

    LaunchedEffect(imageUrl) {
        viewModel.loadImage(imageUrl)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            when (val state = imageState) {
                is ImageState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ImageState.Success -> {
                    val imageBitmap: ImageBitmap? = try {
                        NSFileManager.defaultManager.contentsAtPath(state.imagePath)
                            ?.toByteArray()?.decodeToImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                    if (imageBitmap != null) {
                        ImageViewer(imageBitmap)
                    } else {
                        onDismissRequest()
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

// 扩展NSData转ByteArray
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val buffer = ByteArray(this.length.toInt())
    memScoped {
        val bytes = buffer.refTo(0)
        memcpy(bytes, this@toByteArray.bytes, this@toByteArray.length)
    }
    return buffer
}
