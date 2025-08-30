package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.uikit.loadString
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.rewrite
import kori.composeapp.generated.resources.summarize
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.dragAndDropText(textFieldState: TextFieldState): Modifier {
    val target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                event.items.forEach { item ->
                    item.loadString { s, _ ->
                        s?.let {
                            textFieldState.edit {
                                addInNewLine(s)
                            }
                        }
                    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun Modifier.aiContextMenu(onEvent: (AIContextMenuEvent) -> Unit): Modifier {
    val rewriteLabel = stringResource(Res.string.rewrite)
    val summarizeLabel = stringResource(Res.string.summarize)
    return appendTextContextMenuComponents {
        separator()
        item(key = AIContextMenuEvent.Rewrite, label = rewriteLabel) {
            onEvent(AIContextMenuEvent.Rewrite)
        }
        item(key = AIContextMenuEvent.Summarize, label = summarizeLabel) {
            onEvent(AIContextMenuEvent.Summarize)
        }
        separator()
    }
}