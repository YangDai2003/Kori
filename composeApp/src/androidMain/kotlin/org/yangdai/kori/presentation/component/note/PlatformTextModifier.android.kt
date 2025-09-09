package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.dragAndDropText(textFieldState: TextFieldState): Modifier {
    return this // 有默认实现，无需自定义
}