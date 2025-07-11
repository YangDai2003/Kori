package org.yangdai.kori.presentation.component.note.plaintext

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.outlined.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.EditorRowButton
import org.yangdai.kori.presentation.component.note.EditorRowSection
import org.yangdai.kori.presentation.component.note.platformKeyboardShortCut
import org.yangdai.kori.presentation.component.note.tab
import org.yangdai.kori.presentation.component.note.unTab

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlainTextEditorRow(
    isTemplate: Boolean,
    textFieldState: TextFieldState,
    onEditorRowAction: (EditorRowAction) -> Unit
) = Row(
    modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
) {

    Spacer(Modifier.width(4.dp))

    EditorRowSection {
        EditorRowButton(
            tipText = "$platformKeyboardShortCut + Z",
            icon = Icons.AutoMirrored.Outlined.Undo,
            enabled = textFieldState.undoState.canUndo,
            onClick = { textFieldState.undoState.undo() }
        )

        EditorRowButton(
            tipText = "$platformKeyboardShortCut + Y",
            icon = Icons.AutoMirrored.Outlined.Redo,
            enabled = textFieldState.undoState.canRedo,
            onClick = { textFieldState.undoState.redo() }
        )
    }

    EditorRowSection {
        EditorRowButton(
            icon = Icons.AutoMirrored.Outlined.FormatIndentIncrease,
            onClick = { textFieldState.edit { tab() } }
        )
        EditorRowButton(
            icon = Icons.AutoMirrored.Outlined.FormatIndentDecrease,
            onClick = { textFieldState.edit { unTab() } }
        )
    }

    if (!isTemplate)
        EditorRowSection {
            EditorRowButton(
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                onClick = { onEditorRowAction(EditorRowAction.Templates) }
            )
        }

    Spacer(Modifier.width(4.dp))
}