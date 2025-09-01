package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.dragAndDropText(textFieldState: TextFieldState): Modifier