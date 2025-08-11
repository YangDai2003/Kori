package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditor
import org.yangdai.kori.presentation.component.note.todo.TodoTextEditor

/**
 * A composable function that renders an adaptive editor based on the provided `NoteType`.
 *
 * @param modifier Modifier to be applied to the editor.
 * @param type The type of note, which determines the editor to be displayed (Markdown, Plain Text...).
 * @param textState The state of the text field used in the editor.
 * @param scrollState The scroll state for the editor.
 * @param isReadOnly Whether the editor is in read-only mode.
 * @param isLineNumberVisible Whether line numbers are visible in the editor.
 * @param isLintActive Whether linting is active in the editor.
 * @param headerRange The range of headers for Markdown editor, if applicable.
 * @param findAndReplaceState The state for find-and-replace functionality in the editor.
 */
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
    NoteType.MARKDOWN ->
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

    NoteType.TODO ->
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

    else -> {}
}