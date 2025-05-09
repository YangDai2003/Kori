package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import kfile.PlatformFile

@Composable

expect fun FilePickerDialog(onFilePicked: (PlatformFile?) -> Unit)