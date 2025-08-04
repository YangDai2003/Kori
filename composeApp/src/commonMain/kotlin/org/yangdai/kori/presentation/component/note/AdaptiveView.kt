package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownView
import org.yangdai.kori.presentation.component.note.todo.TodoView

@Composable
fun AdaptiveView(
    modifier: Modifier = Modifier,
    noteType: NoteType,
    contentString: String,
    scrollState: ScrollState,
    isSheetVisible: Boolean = false,
    printTrigger: MutableState<Boolean> = remember { mutableStateOf(false) }
) = when (noteType) {

    NoteType.MARKDOWN -> MarkdownView(
        modifier = modifier,
        html = contentString,
        scrollState = scrollState,
        isSheetVisible = isSheetVisible,
        printTrigger = printTrigger
    )

    NoteType.TODO -> TodoView(
        modifier = modifier,
        todoText = contentString
    )

    else -> {}
}