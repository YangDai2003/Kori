package org.yangdai.kori.presentation.component.note.todo

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
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.completion_mark
import kori.composeapp.generated.resources.context_tag
import kori.composeapp.generated.resources.parentheses
import kori.composeapp.generated.resources.priority_mark
import kori.composeapp.generated.resources.project_tag
import kori.composeapp.generated.resources.redo
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.EditorRowButton
import org.yangdai.kori.presentation.component.note.EditorRowSection
import org.yangdai.kori.presentation.component.note.addBeforeWithWhiteSpace
import org.yangdai.kori.presentation.component.note.parentheses
import org.yangdai.kori.presentation.component.note.platformKeyboardShortCut
import org.yangdai.kori.presentation.component.note.toggleLineStart

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoTextEditorRow(
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
            hint = stringResource(Res.string.undo),
            actionText = "$platformKeyboardShortCut + Z",
            icon = Icons.AutoMirrored.Outlined.Undo,
            enabled = textFieldState.undoState.canUndo,
            onClick = { textFieldState.undoState.undo() }
        )

        EditorRowButton(
            hint = stringResource(Res.string.redo),
            actionText = "$platformKeyboardShortCut + Y",
            icon = Icons.AutoMirrored.Outlined.Redo,
            enabled = textFieldState.undoState.canRedo,
            onClick = { textFieldState.undoState.redo() }
        )
    }

    EditorRowSection {
        EditorRowButton(
            hint = stringResource(Res.string.priority_mark),
            icon = painterResource(Res.drawable.parentheses),
            onClick = { textFieldState.edit { parentheses() } }
        )

        EditorRowButton(
            hint = stringResource(Res.string.project_tag),
            icon = Icons.Outlined.Add,
            onClick = { textFieldState.edit { addBeforeWithWhiteSpace("+") } }
        )

        EditorRowButton(
            hint = stringResource(Res.string.context_tag),
            icon = Icons.Outlined.AlternateEmail,
            onClick = { textFieldState.edit { addBeforeWithWhiteSpace("@") } }
        )

        EditorRowButton(
            hint = stringResource(Res.string.completion_mark),
            icon = Icons.Outlined.CheckBox,
            onClick = { textFieldState.edit { toggleLineStart("x ") } }
        )
    }

    if (!isTemplate)
        EditorRowSection {
            EditorRowButton(
                hint = stringResource(Res.string.templates),
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                onClick = { onEditorRowAction(EditorRowAction.Templates) }
            )
        }

    Spacer(Modifier.width(4.dp))
}