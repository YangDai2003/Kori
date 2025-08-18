package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownView
import org.yangdai.kori.presentation.component.note.todo.TodoView

@Composable
fun AdaptiveView(
    modifier: Modifier,
    noteType: NoteType,
    html: String,
    rawText: String,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>
) = when (noteType) {

    NoteType.MARKDOWN -> MarkdownView(
        modifier = modifier,
        html = html,
        scrollState = scrollState,
        isSheetVisible = isSheetVisible,
        printTrigger = printTrigger
    )

    NoteType.TODO -> TodoView(
        modifier = modifier,
        todoText = rawText
    )

    else -> {}
}