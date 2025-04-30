package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditor

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
) = when (type) {
    NoteType.MARKDOWN -> {
        MarkdownEditor(
            modifier = modifier,
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

    else -> {

    }
}