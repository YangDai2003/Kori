package kink

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.share
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun ShareImageButton(imageBitmap: ImageBitmap) {
    val context = LocalContext.current.applicationContext
    Button(
        onClick = {
            val bitmap = imageBitmap.asAndroidBitmap()
            val file = File(context.cacheDir, "kori_ink_image.png")
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            val fileUri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "image/png"
            }
            context.startActivity(
                Intent.createChooser(shareIntent, null)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    ) {
        Text(stringResource(Res.string.share))
    }
}