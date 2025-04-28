package org.yangdai.kori.presentation.component.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile

@Composable
actual fun FilePickerDialog(onFilePicked: (PickedFile?) -> Unit) {
    val context = LocalContext.current.applicationContext
    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            val documentFile = DocumentFile.fromSingleUri(context, documentUri)
            val pickedFile =
                if (documentFile?.exists() == true && documentFile.canRead() && documentFile.canWrite()) {
                    PickedFile(
                        name = documentFile.name.orEmpty(),
                        path = documentFile.uri.toString(),
                        content = context.contentResolver.openInputStream(documentUri)
                            ?.bufferedReader()?.readText().orEmpty()
                    )
                } else null
            onFilePicked(pickedFile)
        } ?: onFilePicked(null)
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("text/*"))
    }
}