package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditor
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditor
import org.yangdai.kori.presentation.component.note.todo.TodoTextEditor

@Composable
fun AdaptiveEditor(
    modifier: Modifier = Modifier,
    type: NoteType,
    textState: TextFieldState,
    scrollState: ScrollState,
    isReadOnly: Boolean,
    isLineNumberVisible: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState
) = when (type) {
    NoteType.MARKDOWN -> {
        MarkdownEditor(
            modifier = modifier,
            textState = textState,
            scrollState = scrollState,
            headerRange = headerRange,
            editorProperties = EditorProperties(
                isReadOnly = isReadOnly,
                isLineNumberVisible = isLineNumberVisible,
                isLintActive = isLintActive
            ),
            findAndReplaceState = findAndReplaceState
        )
    }

    NoteType.PLAIN_TEXT -> {
        PlainTextEditor(
            modifier = modifier,
            textState = textState,
            scrollState = scrollState,
            editorProperties = EditorProperties(
                isReadOnly = isReadOnly,
                isLineNumberVisible = isLineNumberVisible
            ),
            findAndReplaceState = findAndReplaceState
        )
    }

    NoteType.TODO -> {
        TodoTextEditor(
            modifier = modifier,
            textState = textState,
            scrollState = scrollState,
            editorProperties = EditorProperties(
                isReadOnly = isReadOnly,
                isLineNumberVisible = isLineNumberVisible
            ),
            findAndReplaceState = findAndReplaceState
        )
    }
}