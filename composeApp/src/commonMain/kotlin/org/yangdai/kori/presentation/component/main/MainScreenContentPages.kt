package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.navigation.Screen

@Composable
fun Page(
    notes: List<NoteEntity>,
    contentPadding: PaddingValues,
    noteItemProperties: NoteItemProperties,
    navigateToNote: (String) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) {
    val state = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 360.dp),
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                noteItemProperties = noteItemProperties,
                isSelected = selectedNotes.contains(note.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        // 在多选模式下，点击切换选中状态
                        if (selectedNotes.contains(note.id)) {
                            selectedNotes.remove(note.id)
                        } else {
                            selectedNotes.add(note.id)
                        }
                    } else {
                        // 正常导航到笔记详情
                        navigateToNote(note.id)
                    }
                },
                onLongClick = {
                    // 长按进入多选模式
                    if (!isSelectionMode) {
                        selectedNotes.add(note.id)
                    }
                }
            )
        }
    }
}

@Composable
fun SearchResultsPage(
    keyword: String,
    notes: List<NoteEntity>,
    contentPadding: PaddingValues,
    noteItemProperties: NoteItemProperties,
    navigateToScreen: (Screen) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) {
    val state = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 360.dp),
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            SearchResultNoteItem(
                keyword = keyword,
                note = note,
                noteItemProperties = noteItemProperties,
                isSelected = selectedNotes.contains(note.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        // 在多选模式下，点击切换选中状态
                        if (selectedNotes.contains(note.id)) {
                            selectedNotes.remove(note.id)
                        } else {
                            selectedNotes.add(note.id)
                        }
                    } else {
                        // 正常导航到笔记详情
                        navigateToScreen(Screen.Note(note.id))
                    }
                },
                onLongClick = {
                    // 长按进入多选模式
                    if (!isSelectionMode) {
                        selectedNotes.add(note.id)
                    }
                }
            )
        }
    }
}
