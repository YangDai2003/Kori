package org.yangdai.kori.presentation.component.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kfile.PlatformFile

@Composable
actual fun FilePickerDialog(onFilePicked: (PlatformFile?) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            onFilePicked(PlatformFile(context, documentUri))
        } ?: onFilePicked(null)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("text/*"))
    }
}