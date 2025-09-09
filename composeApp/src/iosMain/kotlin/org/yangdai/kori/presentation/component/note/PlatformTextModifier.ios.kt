package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.uikit.loadString

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.dragAndDropText(textFieldState: TextFieldState): Modifier {
    val target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                event.items[0].loadString { s, _ ->
                    s?.let { textFieldState.edit { addInNewLine(s) } }
                }
                return true
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = target
    )
}