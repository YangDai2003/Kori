package org.yangdai.kori.presentation.component.note.markdown

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
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.automirrored.outlined.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.outlined.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IntegrationInstructions
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.ReportGmailerrorred
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.WarningAmber
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
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.EditorRowButton
import org.yangdai.kori.presentation.component.note.EditorRowSection
import org.yangdai.kori.presentation.component.note.platformKeyboardShortCut

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
                    tipText = "Ctrl + 1",
                    icon = painterResource(Res.drawable.format_h1),
                    onClick = { textFieldState.edit { header(1) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "Ctrl + 2",
                    icon = painterResource(Res.drawable.format_h2),
                    onClick = { textFieldState.edit { header(2) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "Ctrl + 3",
                    icon = painterResource(Res.drawable.format_h3),
                    onClick = { textFieldState.edit { header(3) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "Ctrl + 4",
                    icon = painterResource(Res.drawable.format_h4),
                    onClick = { textFieldState.edit { header(4) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "Ctrl + 5",
                    icon = painterResource(Res.drawable.format_h5),
                    onClick = { textFieldState.edit { header(5) } }
                )
            },
            {
                EditorRowButton(
                    tipText = "Ctrl + 6",
                    icon = painterResource(Res.drawable.format_h6),
                    onClick = { textFieldState.edit { header(6) } }
                )
            }
        )
    }

    EditorRowSection(
        {
            EditorRowButton(
                tipText = "Ctrl + B",
                icon = Icons.Outlined.FormatBold,
                onClick = { textFieldState.edit { bold() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + I",
                icon = Icons.Outlined.FormatItalic,
                onClick = { textFieldState.edit { italic() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + U",
                icon = Icons.Outlined.FormatUnderlined,
                onClick = { textFieldState.edit { underline() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + D",
                icon = Icons.Outlined.StrikethroughS,
                onClick = { textFieldState.edit { strikeThrough() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + H",
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
                tipText = "Ctrl + E",
                icon = Icons.Outlined.Code,
                onClick = { textFieldState.edit { inlineCode() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + Shift + E",
                icon = Icons.Outlined.IntegrationInstructions,
                onClick = { textFieldState.edit { codeBlock() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + M",
                icon = Icons.Outlined.AttachMoney,
                onClick = { textFieldState.edit { inlineMath() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + Shift + M",
                icon = Icons.Outlined.Functions,
                onClick = { textFieldState.edit { mathBlock() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + L",
                icon = Icons.Outlined.Link,
                onClick = { textFieldState.edit { link() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + Q",
                icon = Icons.Outlined.FormatQuote,
                onClick = { textFieldState.edit { quote() } }
            )
        },
    )

    var isAlertSectionExpanded by rememberSaveable { mutableStateOf(false) }
    EditorRowSection(
        {
            EditorRowButton(
                tipText = "Github Alert",
                icon = Icons.AutoMirrored.Outlined.Label,
                onClick = { isAlertSectionExpanded = !isAlertSectionExpanded }
            )
        }
    )

    AnimatedVisibility(visible = isAlertSectionExpanded) {
        EditorRowSection(
            {
                EditorRowButton(
                    tipText = "NOTE",
                    icon = Icons.Outlined.Info,
                    onClick = { textFieldState.edit { alert("NOTE") } }
                )
            },
            {
                EditorRowButton(
                    tipText = "TIP",
                    icon = Icons.Outlined.Lightbulb,
                    onClick = { textFieldState.edit { alert("TIP") } }
                )
            },
            {
                EditorRowButton(
                    tipText = "IMPORTANT",
                    icon = Icons.AutoMirrored.Outlined.Announcement,
                    onClick = { textFieldState.edit { alert("IMPORTANT") } }
                )
            },
            {
                EditorRowButton(
                    tipText = "WARNING",
                    icon = Icons.Outlined.WarningAmber,
                    onClick = { textFieldState.edit { alert("WARNING") } }
                )
            },
            {
                EditorRowButton(
                    tipText = "CAUTION",
                    icon = Icons.Outlined.ReportGmailerrorred,
                    onClick = { textFieldState.edit { alert("CAUTION") } }
                )
            }
        )
    }

    EditorRowSection(
        {
            EditorRowButton(
                tipText = "Ctrl + Shift + B",
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                onClick = { textFieldState.edit { bulletList() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + Shift + N",
                icon = Icons.Outlined.FormatListNumbered,
                onClick = { textFieldState.edit { numberedList() } }
            )
        },
        {
            EditorRowButton(
                tipText = "Ctrl + Shift + T",
                icon = Icons.Outlined.Checklist,
                onClick = { textFieldState.edit { taskList() } }
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
                tipText = "Ctrl + R",
                icon = Icons.Outlined.HorizontalRule,
                onClick = { textFieldState.edit { horizontalRule() } }
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