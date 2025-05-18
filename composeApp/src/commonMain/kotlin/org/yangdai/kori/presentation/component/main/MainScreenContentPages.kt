package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.others
import kori.composeapp.generated.resources.pinned
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.LazyGridScrollbar
import org.yangdai.kori.presentation.navigation.Screen

@Composable
fun GroupedPage(
    notesMap: Map<Boolean, List<NoteEntity>>,
    contentPadding: PaddingValues,
    columns: Int,
    noteItemProperties: NoteItemProperties,
    navigateToScreen: (Screen) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) = Box {
    val state = rememberLazyGridState()
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        columns = GridCells.Fixed(columns),
        state = state,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!notesMap[true].isNullOrEmpty())
            stickyHeader(key = "pinned") {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp, start = 12.dp),
                        text = stringResource(Res.string.pinned),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

        items(notesMap[true] ?: emptyList(), key = { it.id }) { note ->
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

        if (notesMap.size == 2)
            stickyHeader(key = "others") {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp, start = 12.dp),
                        text = stringResource(Res.string.others),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

        items(notesMap[false] ?: emptyList(), key = { it.id }) { note ->
            NoteItem(
                note = note,
                noteItemProperties = noteItemProperties,
                isSelected = selectedNotes.contains(note.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        if (selectedNotes.contains(note.id)) {
                            selectedNotes.remove(note.id)
                        } else {
                            selectedNotes.add(note.id)
                        }
                    } else {
                        navigateToScreen(Screen.Note(note.id))
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        selectedNotes.add(note.id)
                    }
                }
            )
        }
    }
    LazyGridScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        state = state
    )
}

@Composable
fun Page(
    notes: List<NoteEntity>,
    contentPadding: PaddingValues,
    columns: Int,
    noteItemProperties: NoteItemProperties,
    navigateToScreen: (Screen) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) {
    val state = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
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

@Composable
fun TemplatesPage(
    notes: List<NoteEntity>,
    contentPadding: PaddingValues,
    columns: Int,
    noteItemProperties: NoteItemProperties,
    navigateToScreen: (Screen) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) {
    val state = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
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
                        navigateToScreen(Screen.Template(note.id))
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
    columns: Int,
    noteItemProperties: NoteItemProperties,
    navigateToScreen: (Screen) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) {
    val state = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
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
