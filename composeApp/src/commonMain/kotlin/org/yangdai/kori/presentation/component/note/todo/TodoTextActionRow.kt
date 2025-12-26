package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.Composable
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
import org.yangdai.kori.presentation.component.note.Action
import org.yangdai.kori.presentation.component.note.ActionRowScope
import org.yangdai.kori.presentation.component.note.addBeforeWithWhiteSpace
import org.yangdai.kori.presentation.component.note.parentheses
import org.yangdai.kori.presentation.component.note.toggleLineStart

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActionRowScope.TodoTextActionRow(
    textFieldState: TextFieldState,
    onRowAction: (Action) -> Unit
) = ActionRow {
    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.undo),
            actionText = "Z",
            icon = Icons.AutoMirrored.Outlined.Undo,
            enabled = textFieldState.undoState.canUndo,
            onClick = { textFieldState.undoState.undo() }
        )
        ActionButton(
            hint = stringResource(Res.string.redo),
            actionText = "Y",
            icon = Icons.AutoMirrored.Outlined.Redo,
            enabled = textFieldState.undoState.canRedo,
            onClick = { textFieldState.undoState.redo() }
        )
    }

    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.priority_mark),
            icon = painterResource(Res.drawable.parentheses),
            onClick = { textFieldState.edit { parentheses() } }
        )
        ActionButton(
            hint = stringResource(Res.string.project_tag),
            icon = Icons.Outlined.Add,
            onClick = { textFieldState.edit { addBeforeWithWhiteSpace("+") } }
        )
        ActionButton(
            hint = stringResource(Res.string.context_tag),
            icon = Icons.Outlined.AlternateEmail,
            onClick = { textFieldState.edit { addBeforeWithWhiteSpace("@") } }
        )
        ActionButton(
            hint = stringResource(Res.string.completion_mark),
            icon = Icons.Outlined.CheckBox,
            onClick = { textFieldState.edit { toggleLineStart("x ") } }
        )
    }

    if (!isTemplateActionRow)
        ActionRowSection {
            ActionButton(
                hint = stringResource(Res.string.templates),
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                onClick = { onRowAction(Action.Templates) }
            )
        }
}