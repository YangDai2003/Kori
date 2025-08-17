package org.yangdai.kori.presentation.component.note.plaintext

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.yangdai.kori.presentation.component.note.EditorProperties
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.TextEditor
import org.yangdai.kori.presentation.component.note.dragAndDropText

@Composable
fun PlainTextEditor(
    modifier: Modifier = Modifier,
    textState: TextFieldState,
    scrollState: ScrollState,
    editorProperties: EditorProperties,
    findAndReplaceState: FindAndReplaceState
) = TextEditor(
    modifier = modifier,
    textFieldModifier = Modifier.plainTextKeyEvents(textState).dragAndDropText(textState),
    textState = textState,
    scrollState = scrollState,
    findAndReplaceState = findAndReplaceState,
    editorProperties = editorProperties,
    outputTransformation = remember { PlainTextTransformation() }
)