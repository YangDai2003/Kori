package org.yangdai.kori.presentation.component.note

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.dragAndDropText(onDrop: (String) -> Unit): Modifier {
    return this // 有默认实现，无需自定义
}