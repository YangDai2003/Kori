package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.EditorScrollbar
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditor
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditor

@Composable
fun Editor(
    modifier: Modifier = Modifier,
    type: NoteType,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit
) = Box(modifier) {
    when (type) {
        NoteType.MARKDOWN -> {
            MarkdownEditor(
                modifier = Modifier.fillMaxSize(),
                state = state,
                scrollState = scrollState,
                readMode = readMode,
                showLineNumbers = showLineNumbers,
                isLintActive = isLintActive,
                headerRange = headerRange,
                findAndReplaceState = findAndReplaceState,
                onFindAndReplaceUpdate = onFindAndReplaceUpdate
            )
        }

        NoteType.PLAIN_TEXT -> {
            PlainTextEditor(
                modifier = Modifier.fillMaxSize(),
                state = state,
                scrollState = scrollState,
                readMode = readMode,
                showLineNumbers = showLineNumbers,
                findAndReplaceState = findAndReplaceState,
                onFindAndReplaceUpdate = onFindAndReplaceUpdate
            )
        }
    }
    EditorScrollbar(Modifier.align(Alignment.CenterEnd).fillMaxHeight(), scrollState)
}