package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.TextEditor
import org.yangdai.kori.presentation.component.note.dragAndDropText

@Composable
fun MarkdownEditor(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit
) = TextEditor(
    modifier = modifier,
    textFieldModifier = Modifier
        .padding(start = if (showLineNumbers) 4.dp else 16.dp, end = 16.dp)
        .fillMaxSize()
        .markdownKeyEvents(state)
        .dragAndDropText(state),
    state = state,
    scrollState = scrollState,
    readMode = readMode,
    showLineNumbers = showLineNumbers,
    headerRange = headerRange,
    isLintActive = isLintActive,
    outputTransformation = remember { MarkdownTransformation() },
    findAndReplaceState = findAndReplaceState,
    onFindAndReplaceUpdate = onFindAndReplaceUpdate
)