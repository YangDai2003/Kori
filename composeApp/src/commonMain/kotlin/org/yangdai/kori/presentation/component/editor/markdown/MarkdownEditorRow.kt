package org.yangdai.kori.presentation.component.editor.markdown

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.IntegrationInstructions
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.Title
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.format_h1
import kori.composeapp.generated.resources.format_h2
import kori.composeapp.generated.resources.format_h3
import kori.composeapp.generated.resources.format_h4
import kori.composeapp.generated.resources.format_h5
import kori.composeapp.generated.resources.format_h6
import kori.composeapp.generated.resources.parentheses
import org.jetbrains.compose.resources.painterResource
import org.yangdai.kori.presentation.component.editor.EditorRowAction
import org.yangdai.kori.presentation.component.editor.EditorRowButton
import org.yangdai.kori.presentation.component.editor.EditorRowSection
import org.yangdai.kori.presentation.component.editor.platformKeyboardShortCut

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarkdownEditorRow(
    textFieldState: TextFieldState,
    onEditorRowAction: (EditorRowAction) -> Unit
) = Row(
    modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
) {

    Spacer(Modifier.width(4.dp))

    EditorRowSection(
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + Z",
                icon = Icons.AutoMirrored.Outlined.Undo,
                enabled = textFieldState.undoState.canUndo,
                onClick = { textFieldState.undoState.undo() }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + Y",
                icon = Icons.AutoMirrored.Outlined.Redo,
                enabled = textFieldState.undoState.canRedo,
                onClick = { textFieldState.undoState.redo() }
            )
        }
    )

    var isHeadingSectionExpanded by rememberSaveable { mutableStateOf(false) }

    EditorRowSection(
        {
            EditorRowButton(
                icon = Icons.Outlined.Title,
                onClick = { isHeadingSectionExpanded = !isHeadingSectionExpanded }
            )
        }
    )

    AnimatedVisibility(visible = isHeadingSectionExpanded) {
        EditorRowSection(
            {
                EditorRowButton(
                    tipText = "$platformKeyboardShortCut + 1",
                    icon = painterResource(Res.drawable.format_h1),
                    onClick = { textFieldState.edit { addHeader(1) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "$platformKeyboardShortCut + 2",
                    icon = painterResource(Res.drawable.format_h2),
                    onClick = { textFieldState.edit { addHeader(2) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "$platformKeyboardShortCut + 3",
                    icon = painterResource(Res.drawable.format_h3),
                    onClick = { textFieldState.edit { addHeader(3) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "$platformKeyboardShortCut + 4",
                    icon = painterResource(Res.drawable.format_h4),
                    onClick = { textFieldState.edit { addHeader(4) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "$platformKeyboardShortCut + 5",
                    icon = painterResource(Res.drawable.format_h5),
                    onClick = { textFieldState.edit { addHeader(5) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "$platformKeyboardShortCut + 6",
                    icon = painterResource(Res.drawable.format_h6),
                    onClick = { textFieldState.edit { addHeader(6) } }
                )
            }
        )
    }

    EditorRowSection(
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + B",
                icon = Icons.Outlined.FormatBold,
                onClick = { textFieldState.edit { bold() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + I",
                icon = Icons.Outlined.FormatItalic,
                onClick = { textFieldState.edit { italic() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + U",
                icon = Icons.Outlined.FormatUnderlined,
                onClick = { textFieldState.edit { underline() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + D",
                icon = Icons.Outlined.StrikethroughS,
                onClick = { textFieldState.edit { strikeThrough() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + H",
                icon = Icons.Outlined.FormatPaint,
                onClick = { textFieldState.edit { highlight() } }
            )
        }
    )

    EditorRowSection(
        {
            EditorRowButton(
                icon = Icons.AutoMirrored.Outlined.FormatIndentIncrease,
                onClick = { textFieldState.edit { tab() } }
            )
        },
        {
            EditorRowButton(
                icon = Icons.AutoMirrored.Outlined.FormatIndentDecrease,
                onClick = { textFieldState.edit { unTab() } }
            )
        }
    )

    EditorRowSection(
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + E",
                icon = Icons.Outlined.Code,
                onClick = { textFieldState.edit { inlineCode() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + Shift + E",
                icon = Icons.Outlined.IntegrationInstructions,
                onClick = { textFieldState.edit { codeBlock() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + Q",
                icon = Icons.Outlined.FormatQuote,
                onClick = { textFieldState.edit { quote() } }
            )
        }
    )

    EditorRowSection(
        {
            EditorRowButton(
                icon = painterResource(Res.drawable.parentheses),
                onClick = { textFieldState.edit { parentheses() } }
            )
        },
        {
            EditorRowButton(
                icon = Icons.Outlined.DataArray,
                onClick = { textFieldState.edit { brackets() } }
            )
        },
        {
            EditorRowButton(
                icon = Icons.Outlined.DataObject,
                onClick = { textFieldState.edit { braces() } }
            )
        },
        {
            EditorRowButton(
                tipText = "$platformKeyboardShortCut + R",
                icon = Icons.Outlined.HorizontalRule,
                onClick = { textFieldState.edit { addRule() } }
            )
        }
    )

    EditorRowSection(
        {
            EditorRowButton(
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                onClick = { onEditorRowAction(EditorRowAction.Templates) }
            )
        }
    )

    Spacer(Modifier.width(4.dp))
}