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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Info
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
import kori.composeapp.generated.resources.mermaid_diagram
import kori.composeapp.generated.resources.numbered_list
import kori.composeapp.generated.resources.photo_library_24px
import kori.composeapp.generated.resources.quote
import kori.composeapp.generated.resources.redo
import kori.composeapp.generated.resources.strikethrough
import kori.composeapp.generated.resources.table
import kori.composeapp.generated.resources.table_24px
import kori.composeapp.generated.resources.task_list
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.underline
import kori.composeapp.generated.resources.undo
import kori.composeapp.generated.resources.video_library_24px
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.Action
import org.yangdai.kori.presentation.component.note.ActionRowScope
import org.yangdai.kori.presentation.component.note.addAfter
import org.yangdai.kori.presentation.component.note.alert
import org.yangdai.kori.presentation.component.note.bold
import org.yangdai.kori.presentation.component.note.bulletList
import org.yangdai.kori.presentation.component.note.code
import org.yangdai.kori.presentation.component.note.header
import org.yangdai.kori.presentation.component.note.highlight
import org.yangdai.kori.presentation.component.note.horizontalRule
import org.yangdai.kori.presentation.component.note.italic
import org.yangdai.kori.presentation.component.note.link
import org.yangdai.kori.presentation.component.note.math
import org.yangdai.kori.presentation.component.note.mermaidDiagram
import org.yangdai.kori.presentation.component.note.numberedList
import org.yangdai.kori.presentation.component.note.quote
import org.yangdai.kori.presentation.component.note.strikeThrough
import org.yangdai.kori.presentation.component.note.tab
import org.yangdai.kori.presentation.component.note.taskList
import org.yangdai.kori.presentation.component.note.unTab
import org.yangdai.kori.presentation.component.note.underline

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActionRowScope.MarkdownEditorRow(
    isTemplate: Boolean,
    textFieldState: TextFieldState,
    onRowAction: (Action) -> Unit
) = Row(
    modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
) {

    Spacer(Modifier.width(4.dp))

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

    var isHeadingSectionExpanded by rememberSaveable { mutableStateOf(false) }
    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.heading),
            icon = Icons.Outlined.Title,
            onClick = { isHeadingSectionExpanded = !isHeadingSectionExpanded }
        )
    }

    AnimatedVisibility(visible = isHeadingSectionExpanded) {
        ActionRowSection {
            ActionButton(
                actionText = "1",
                icon = painterResource(Res.drawable.format_h1),
                onClick = { textFieldState.edit { header(1) } }
            )
            ActionButton(
                actionText = "2",
                icon = painterResource(Res.drawable.format_h2),
                onClick = { textFieldState.edit { header(2) } }
            )
            ActionButton(
                actionText = "3",
                icon = painterResource(Res.drawable.format_h3),
                onClick = { textFieldState.edit { header(3) } }
            )
            ActionButton(
                actionText = "4",
                icon = painterResource(Res.drawable.format_h4),
                onClick = { textFieldState.edit { header(4) } }
            )
            ActionButton(
                actionText = "5",
                icon = painterResource(Res.drawable.format_h5),
                onClick = { textFieldState.edit { header(5) } }
            )
            ActionButton(
                actionText = "6",
                icon = painterResource(Res.drawable.format_h6),
                onClick = { textFieldState.edit { header(6) } }
            )
        }
    }

    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.bold),
            actionText = "B",
            icon = Icons.Outlined.FormatBold,
            onClick = { textFieldState.edit { bold() } }
        )
        ActionButton(
            hint = stringResource(Res.string.italic),
            actionText = "I",
            icon = Icons.Outlined.FormatItalic,
            onClick = { textFieldState.edit { italic() } }
        )
        ActionButton(
            hint = stringResource(Res.string.strikethrough),
            actionText = "D",
            icon = Icons.Outlined.StrikethroughS,
            onClick = { textFieldState.edit { strikeThrough() } }
        )
        ActionButton(
            hint = stringResource(Res.string.underline),
            actionText = "U",
            icon = Icons.Outlined.FormatUnderlined,
            onClick = { textFieldState.edit { underline() } }
        )
        ActionButton(
            hint = stringResource(Res.string.highlight),
            actionText = "H",
            icon = Icons.Outlined.FormatPaint,
            onClick = { textFieldState.edit { highlight() } }
        )
    }

    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.indent_increase),
            icon = Icons.AutoMirrored.Outlined.FormatIndentIncrease,
            onClick = { textFieldState.edit { tab() } }
        )
        ActionButton(
            hint = stringResource(Res.string.indent_decrease),
            icon = Icons.AutoMirrored.Outlined.FormatIndentDecrease,
            onClick = { textFieldState.edit { unTab() } }
        )
    }

    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.code),
            actionText = "E",
            icon = Icons.Outlined.Code,
            onClick = { textFieldState.edit { code() } }
        )
        ActionButton(
            hint = stringResource(Res.string.math),
            actionText = "M",
            icon = Icons.Outlined.Functions,
            onClick = { textFieldState.edit { math() } }
        )
        ActionButton(
            hint = stringResource(Res.string.link),
            actionText = "L",
            icon = Icons.Outlined.Link,
            onClick = { textFieldState.edit { link() } }
        )
        ActionButton(
            hint = stringResource(Res.string.quote),
            actionText = "Q",
            icon = Icons.Outlined.FormatQuote,
            onClick = { textFieldState.edit { quote() } }
        )
    }

    var isAlertSectionExpanded by rememberSaveable { mutableStateOf(false) }
    ActionRowSection {
        ActionButton(
            hint = "Github Alert",
            icon = Icons.AutoMirrored.Outlined.Label,
            onClick = { isAlertSectionExpanded = !isAlertSectionExpanded }
        )
    }

    AnimatedVisibility(visible = isAlertSectionExpanded) {
        ActionRowSection {
            ActionButton(
                hint = "NOTE",
                icon = Icons.Outlined.Info,
                onClick = { textFieldState.edit { alert("NOTE") } }
            )
            ActionButton(
                hint = "TIP",
                icon = Icons.Outlined.Lightbulb,
                onClick = { textFieldState.edit { alert("TIP") } }
            )
            ActionButton(
                hint = "IMPORTANT",
                icon = Icons.AutoMirrored.Outlined.Announcement,
                onClick = { textFieldState.edit { alert("IMPORTANT") } }
            )
            ActionButton(
                hint = "WARNING",
                icon = Icons.Outlined.WarningAmber,
                onClick = { textFieldState.edit { alert("WARNING") } }
            )
            ActionButton(
                hint = "CAUTION",
                icon = Icons.Outlined.ReportGmailerrorred,
                onClick = { textFieldState.edit { alert("CAUTION") } }
            )
        }
    }

    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.bulleted_list),
            actionText = "⇧ + B",
            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            onClick = { textFieldState.edit { bulletList() } }
        )
        ActionButton(
            hint = stringResource(Res.string.numbered_list),
            actionText = "⇧ + N",
            icon = Icons.Outlined.FormatListNumbered,
            onClick = { textFieldState.edit { numberedList() } }
        )
        ActionButton(
            hint = stringResource(Res.string.task_list),
            actionText = "⇧ + T",
            icon = Icons.Outlined.Checklist,
            onClick = { textFieldState.edit { taskList() } }
        )
    }

    ActionRowSection {
        ActionButton(
            hint = stringResource(Res.string.horizontal_rule),
            actionText = "R",
            icon = Icons.Outlined.HorizontalRule,
            onClick = { textFieldState.edit { horizontalRule() } }
        )
        ActionButton(
            hint = stringResource(Res.string.mermaid_diagram),
            actionText = "⇧ + D",
            icon = Icons.Outlined.AddChart,
            onClick = { textFieldState.edit { mermaidDiagram() } }
        )
        ActionButton(
            hint = stringResource(Res.string.table),
            actionText = "T",
            icon = painterResource(Res.drawable.table_24px),
            onClick = { onRowAction(Action.Table) }
        )
    }

    if (!isTemplate)
        ActionRowSection {
            ActionButton(
                icon = painterResource(Res.drawable.photo_library_24px),
                onClick = { onRowAction(Action.Images) }
            )
            ActionButton(
                icon = painterResource(Res.drawable.video_library_24px),
                onClick = { onRowAction(Action.Video) }
            )
            ActionButton(
                icon = painterResource(Res.drawable.library_music_24px),
                onClick = { onRowAction(Action.Audio) }
            )
            ActionButton(
                hint = stringResource(Res.string.templates),
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                onClick = { onRowAction(Action.Templates) }
            )
        }
    else
        ActionRowSection {
            ActionButton(
                icon = Icons.Outlined.CalendarToday,
                onClick = { textFieldState.edit { addAfter("{{date}}") } }
            )
            ActionButton(
                icon = Icons.Outlined.AccessTime,
                onClick = { textFieldState.edit { addAfter("{{time}}") } }
            )
        }

    Spacer(Modifier.width(4.dp))
}