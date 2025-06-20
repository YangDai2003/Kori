package org.yangdai.kori.presentation.component.note.todo

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
fun TodoTextEditor(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    findAndReplaceState: FindAndReplaceState
) = TextEditor(
    modifier = modifier,
    textFieldModifier = Modifier
        .padding(start = if (showLineNumbers) 4.dp else 16.dp, end = 16.dp)
        .fillMaxSize()
        .todoTextKeyEvents(state)
        .dragAndDropText(state),
    state = state,
    scrollState = scrollState,
    findAndReplaceState = findAndReplaceState,
    readMode = readMode,
    showLineNumbers = showLineNumbers,
    outputTransformation = remember { TodoTransformation() }
)