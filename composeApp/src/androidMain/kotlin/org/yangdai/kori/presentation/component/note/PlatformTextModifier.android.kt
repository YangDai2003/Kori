package org.yangdai.kori.presentation.component.note

import android.content.ClipDescription
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.rewrite
import kori.composeapp.generated.resources.summarize
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun Modifier.dragAndDropText(textFieldState: TextFieldState): Modifier {
    val target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                textFieldState.edit { addInNewLine(event.toAndroidDragEvent().clipData.getItemAt(0).text.toString()) }
                return true
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { event ->
            event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
        }, target = target
    )
}

@Composable
actual fun Modifier.aiContextMenu(
    enabled: Boolean,
    onEvent: (AIContextMenuEvent) -> Unit
): Modifier {
    val rewriteLabel = stringResource(Res.string.rewrite)
    val summarizeLabel = stringResource(Res.string.summarize)
    return appendTextContextMenuComponents {
        if (!enabled) return@appendTextContextMenuComponents
        separator()
        item(key = AIContextMenuEvent.Rewrite, label = rewriteLabel) {
            onEvent(AIContextMenuEvent.Rewrite)
        }
        separator()
        item(key = AIContextMenuEvent.Summarize, label = summarizeLabel) {
            onEvent(AIContextMenuEvent.Summarize)
        }
        separator()
    }
}