package org.yangdai.kori.presentation.component.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.dragAndDropText(state: TextFieldState): Modifier