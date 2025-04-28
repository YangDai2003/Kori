package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun FilePickerDialog(onFilePicked: (PickedFile?) -> Unit) {
    val fileDialog = java.awt.FileDialog(
        null as java.awt.Frame?,
        stringResource(Res.string.app_name),
        java.awt.FileDialog.LOAD
    ).apply {
        file = "*.txt;*.md"
        isVisible = true
    }

    if (fileDialog.file != null && fileDialog.directory != null) {
        val file = java.io.File(fileDialog.directory, fileDialog.file)
        if (file.exists() && (file.extension == "txt" || file.extension == "md")) {
            val content = file.readText()
            onFilePicked(PickedFile(name = file.name, path = file.absolutePath, content = content))
        } else {
            onFilePicked(null)
        }
    } else {
        onFilePicked(null)
    }
}