package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.dragAndDropText(state: TextFieldState): Modifier = this