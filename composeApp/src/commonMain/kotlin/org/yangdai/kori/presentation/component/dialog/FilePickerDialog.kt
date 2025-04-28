package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable

data class PickedFile(val name: String, val path: String, val content: String)

@Composable
expect fun FilePickerDialog(onFilePicked: (PickedFile?) -> Unit)