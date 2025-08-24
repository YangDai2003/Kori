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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AddChart
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarToday
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
import kori.composeapp.generated.resources.bold
import kori.composeapp.generated.resources.bulleted_list
import kori.composeapp.generated.resources.code
import kori.composeapp.generated.resources.code_block
import kori.composeapp.generated.resources.format_h1
import kori.composeapp.generated.resources.format_h2
import kori.composeapp.generated.resources.format_h3
import kori.composeapp.generated.resources.format_h4
import kori.composeapp.generated.resources.format_h5
import kori.composeapp.generated.resources.format_h6
import kori.composeapp.generated.resources.heading
import kori.composeapp.generated.resources.highlight
import kori.composeapp.generated.resources.horizontal_rule
import kori.composeapp.generated.resources.indent_decrease
import kori.composeapp.generated.resources.indent_increase
import kori.composeapp.generated.resources.italic
import kori.composeapp.generated.resources.library_music_24px
import kori.composeapp.generated.resources.link
import kori.composeapp.generated.resources.math
import kori.composeapp.generated.resources.math_block
import kori.composeapp.generated.resources.mermaid_diagram
import kori.composeapp.generated.resources.numbered_list
import kori.composeapp.generated.resources.parentheses
import kori.composeapp.generated.resources.photo_library_24px
import kori.composeapp.generated.resources.quote
import kori.composeapp.generated.resources.redo
import kori.composeapp.generated.resources.strikethrough
import kori.composeapp.generated.resources.task_list
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.underline
import kori.composeapp.generated.resources.undo
import kori.composeapp.generated.resources.video_library_24px
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.EditorRowScope
import org.yangdai.kori.presentation.component.note.addAfter
import org.yangdai.kori.presentation.component.note.alert
import org.yangdai.kori.presentation.component.note.bold
import org.yangdai.kori.presentation.component.note.braces
import org.yangdai.kori.presentation.component.note.brackets
import org.yangdai.kori.presentation.component.note.bulletList
import org.yangdai.kori.presentation.component.note.codeBlock
import org.yangdai.kori.presentation.component.note.header
import org.yangdai.kori.presentation.component.note.highlight
import org.yangdai.kori.presentation.component.note.horizontalRule
import org.yangdai.kori.presentation.component.note.inlineCode
import org.yangdai.kori.presentation.component.note.inlineMath
import org.yangdai.kori.presentation.component.note.italic
import org.yangdai.kori.presentation.component.note.link
import org.yangdai.kori.presentation.component.note.mathBlock
import org.yangdai.kori.presentation.component.note.mermaidDiagram
import org.yangdai.kori.presentation.component.note.numberedList
import org.yangdai.kori.presentation.component.note.parentheses
import org.yangdai.kori.presentation.component.note.platformKeyboardShortCut
import org.yangdai.kori.presentation.component.note.quote
import org.yangdai.kori.presentation.component.note.strikeThrough
import org.yangdai.kori.presentation.component.note.tab
import org.yangdai.kori.presentation.component.note.taskList
import org.yangdai.kori.presentation.component.note.unTab
import org.yangdai.kori.presentation.component.note.underline

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorRowScope.MarkdownEditorRow(
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

    var isHeadingSectionExpanded by rememberSaveable { mutableStateOf(false) }
    EditorRowSection {
        EditorRowButton(
            hint = stringResource(Res.string.heading),
            icon = Icons.Outlined.Title,
            onClick = { isHeadingSectionExpanded = !isHeadingSectionExpanded }
        )
    }

    AnimatedVisibility(visible = isHeadingSectionExpanded) {
        EditorRowSection {
            EditorRowButton(
                actionText = "Ctrl + 1",
                icon = painterResource(Res.drawable.format_h1),
                onClick = { textFieldState.edit { header(1) } }
            )
            EditorRowButton(
                actionText = "Ctrl + 2",
                icon = painterResource(Res.drawable.format_h2),
                onClick = { textFieldState.edit { header(2) } }
            )
            EditorRowButton(
                actionText = "Ctrl + 3",
                icon = painterResource(Res.drawable.format_h3),
                onClick = { textFieldState.edit { header(3) } }
            )
            EditorRowButton(
                actionText = "Ctrl + 4",
                icon = painterResource(Res.drawable.format_h4),
                onClick = { textFieldState.edit { header(4) } }
            )
            EditorRowButton(
                actionText = "Ctrl + 5",
                icon = painterResource(Res.drawable.format_h5),
                onClick = { textFieldState.edit { header(5) } }
            )
            EditorRowButton(
                actionText = "Ctrl + 6",
                icon = painterResource(Res.drawable.format_h6),
                onClick = { textFieldState.edit { header(6) } }
            )
        }
    }

    EditorRowSection {
        EditorRowButton(
            hint = stringResource(Res.string.bold),
            actionText = "Ctrl + B",
            icon = Icons.Outlined.FormatBold,
            onClick = { textFieldState.edit { bold() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.italic),
            actionText = "Ctrl + I",
            icon = Icons.Outlined.FormatItalic,
            onClick = { textFieldState.edit { italic() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.strikethrough),
            actionText = "Ctrl + D",
            icon = Icons.Outlined.StrikethroughS,
            onClick = { textFieldState.edit { strikeThrough() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.underline),
            actionText = "Ctrl + U",
            icon = Icons.Outlined.FormatUnderlined,
            onClick = { textFieldState.edit { underline() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.highlight),
            actionText = "Ctrl + H",
            icon = Icons.Outlined.FormatPaint,
            onClick = { textFieldState.edit { highlight() } }
        )
    }

    EditorRowSection {
        EditorRowButton(
            hint = stringResource(Res.string.indent_increase),
            icon = Icons.AutoMirrored.Outlined.FormatIndentIncrease,
            onClick = { textFieldState.edit { tab() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.indent_decrease),
            icon = Icons.AutoMirrored.Outlined.FormatIndentDecrease,
            onClick = { textFieldState.edit { unTab() } }
        )
    }

    EditorRowSection {
        EditorRowButton(
            hint = stringResource(Res.string.code),
            actionText = "Ctrl + E",
            icon = Icons.Outlined.Code,
            onClick = { textFieldState.edit { inlineCode() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.code_block),
            actionText = "Ctrl + Shift + E",
            icon = Icons.Outlined.IntegrationInstructions,
            onClick = { textFieldState.edit { codeBlock() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.math),
            actionText = "Ctrl + M",
            icon = Icons.Outlined.AttachMoney,
            onClick = { textFieldState.edit { inlineMath() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.math_block),
            actionText = "Ctrl + Shift + M",
            icon = Icons.Outlined.Functions,
            onClick = { textFieldState.edit { mathBlock() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.link),
            actionText = "Ctrl + L",
            icon = Icons.Outlined.Link,
            onClick = { textFieldState.edit { link() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.quote),
            actionText = "Ctrl + Q",
            icon = Icons.Outlined.FormatQuote,
            onClick = { textFieldState.edit { quote() } }
        )
    }

    var isAlertSectionExpanded by rememberSaveable { mutableStateOf(false) }
    EditorRowSection {
        EditorRowButton(
            hint = "Github Alert",
            icon = Icons.AutoMirrored.Outlined.Label,
            onClick = { isAlertSectionExpanded = !isAlertSectionExpanded }
        )
    }

    AnimatedVisibility(visible = isAlertSectionExpanded) {
        EditorRowSection {
            EditorRowButton(
                hint = "NOTE",
                icon = Icons.Outlined.Info,
                onClick = { textFieldState.edit { alert("NOTE") } }
            )
            EditorRowButton(
                hint = "TIP",
                icon = Icons.Outlined.Lightbulb,
                onClick = { textFieldState.edit { alert("TIP") } }
            )
            EditorRowButton(
                hint = "IMPORTANT",
                icon = Icons.AutoMirrored.Outlined.Announcement,
                onClick = { textFieldState.edit { alert("IMPORTANT") } }
            )
            EditorRowButton(
                hint = "WARNING",
                icon = Icons.Outlined.WarningAmber,
                onClick = { textFieldState.edit { alert("WARNING") } }
            )
            EditorRowButton(
                hint = "CAUTION",
                icon = Icons.Outlined.ReportGmailerrorred,
                onClick = { textFieldState.edit { alert("CAUTION") } }
            )
        }
    }

    EditorRowSection {
        EditorRowButton(
            hint = stringResource(Res.string.bulleted_list),
            actionText = "Ctrl + Shift + B",
            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            onClick = { textFieldState.edit { bulletList() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.numbered_list),
            actionText = "Ctrl + Shift + N",
            icon = Icons.Outlined.FormatListNumbered,
            onClick = { textFieldState.edit { numberedList() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.task_list),
            actionText = "Ctrl + Shift + T",
            icon = Icons.Outlined.Checklist,
            onClick = { textFieldState.edit { taskList() } }
        )
    }

    EditorRowSection {
        EditorRowButton(
            icon = painterResource(Res.drawable.parentheses),
            onClick = { textFieldState.edit { parentheses() } }
        )
        EditorRowButton(
            icon = Icons.Outlined.DataArray,
            onClick = { textFieldState.edit { brackets() } }
        )
        EditorRowButton(
            icon = Icons.Outlined.DataObject,
            onClick = { textFieldState.edit { braces() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.horizontal_rule),
            actionText = "Ctrl + R",
            icon = Icons.Outlined.HorizontalRule,
            onClick = { textFieldState.edit { horizontalRule() } }
        )
        EditorRowButton(
            hint = stringResource(Res.string.mermaid_diagram),
            actionText = "Ctrl + Shift + D",
            icon = Icons.Outlined.AddChart,
            onClick = { textFieldState.edit { mermaidDiagram() } }
        )
    }

    if (!isTemplate)
        EditorRowSection {
            EditorRowButton(
                icon = painterResource(Res.drawable.photo_library_24px),
                onClick = { onEditorRowAction(EditorRowAction.Images) }
            )
            EditorRowButton(
                icon = painterResource(Res.drawable.video_library_24px),
                onClick = { onEditorRowAction(EditorRowAction.Videos) }
            )
            EditorRowButton(
                icon = painterResource(Res.drawable.library_music_24px),
                onClick = { onEditorRowAction(EditorRowAction.Audio) }
            )
            EditorRowButton(
                hint = stringResource(Res.string.templates),
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                onClick = { onEditorRowAction(EditorRowAction.Templates) }
            )
        }
    else
        EditorRowSection {
            EditorRowButton(
                icon = Icons.Outlined.CalendarToday,
                onClick = { textFieldState.edit { addAfter("{{date}}") } }
            )
            EditorRowButton(
                icon = Icons.Outlined.AccessTime,
                onClick = { textFieldState.edit { addAfter("{{time}}") } }
            )
        }

    Spacer(Modifier.width(4.dp))
}