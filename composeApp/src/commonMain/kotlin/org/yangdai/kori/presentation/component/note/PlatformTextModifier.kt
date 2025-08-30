package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.dragAndDropText(textFieldState: TextFieldState): Modifier

sealed interface AIContextMenuEvent {
    data object Rewrite : AIContextMenuEvent
    data object Summarize : AIContextMenuEvent
}

@Composable
expect fun Modifier.aiContextMenu(onEvent: (AIContextMenuEvent) -> Unit): Modifier