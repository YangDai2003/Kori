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
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection

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
        return url.startsWith("file://") || File(url).exists()
    }

    private fun getLocalFilePath(fileUrl: String): String {
        return if (fileUrl.startsWith("file://")) fileUrl.removePrefix("file://") else fileUrl
    }

    private suspend fun downloadAndSaveImage(imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            val tmpDir = System.getProperty("java.io.tmpdir")
            val koriDir = File(tmpDir, ".kori")
            val imgDir = File(koriDir, "img")
            if (!imgDir.exists()) imgDir.mkdirs()
            val fileName = "img_${System.currentTimeMillis()}"
            val file = File(imgDir, fileName)
            val conn = imageUrl.toHttpUrl().toUrl().openConnection() as HttpURLConnection
            conn.connect()
            conn.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        }
    }

    fun clearCache() {
        val userHome = System.getProperty("user.home")
        val imgDir = File(File(userHome, ".kori"), "img")
        imgDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("img_")) file.delete()
        }
    }
}

@Composable
actual fun ImageViewerDialog(imageUrl: String, onDismissRequest: () -> Unit) {
    val viewModel = viewModel<ImageViewModel>()
    val imageState by viewModel.imageState.collectAsStateWithLifecycle()

    DisposableEffect(imageUrl) {
        viewModel.loadImage(imageUrl)
        onDispose {
            viewModel.clearCache()
        }
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
                        File(state.imagePath).inputStream().readAllBytes().decodeToImageBitmap()
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
                is ImageState.Empty -> {}
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