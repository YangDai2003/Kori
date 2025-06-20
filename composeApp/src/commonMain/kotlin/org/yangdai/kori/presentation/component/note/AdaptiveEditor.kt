package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditor
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditor
import org.yangdai.kori.presentation.component.note.template.TemplateEditor
import org.yangdai.kori.presentation.component.note.todo.TodoTextEditor

@Composable
fun AdaptiveEditor(
    modifier: Modifier = Modifier,
    isTemplate: Boolean = false,
    type: NoteType,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState
) = if (isTemplate) TemplateEditor(
    modifier = modifier,
    state = state,
    scrollState = scrollState,
    readMode = readMode,
    showLineNumbers = showLineNumbers,
    findAndReplaceState = findAndReplaceState
) else when (type) {
    NoteType.MARKDOWN -> {
        MarkdownEditor(
            modifier = modifier,
            state = state,
            scrollState = scrollState,
            readMode = readMode,
            showLineNumbers = showLineNumbers,
            isLintActive = isLintActive,
            headerRange = headerRange,
            findAndReplaceState = findAndReplaceState
        )
    }

    NoteType.PLAIN_TEXT -> {
        PlainTextEditor(
            modifier = modifier,
            state = state,
            scrollState = scrollState,
            readMode = readMode,
            showLineNumbers = showLineNumbers,
            findAndReplaceState = findAndReplaceState
        )
    }

    NoteType.TODO -> {
        TodoTextEditor(
            modifier = modifier,
            state = state,
            scrollState = scrollState,
            readMode = readMode,
            showLineNumbers = showLineNumbers,
            findAndReplaceState = findAndReplaceState
        )
    }
}