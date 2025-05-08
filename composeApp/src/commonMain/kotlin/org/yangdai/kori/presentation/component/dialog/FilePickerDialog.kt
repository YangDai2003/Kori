package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

// TODO 重构文件实现，实现文件单个，多个选取，文件读取和写入。
@Serializable
data class PickedFile(val name: String = "", val path: String = "", val content: String = "")

@Composable
expect fun FilePickerDialog(onFilePicked: (PickedFile?) -> Unit)