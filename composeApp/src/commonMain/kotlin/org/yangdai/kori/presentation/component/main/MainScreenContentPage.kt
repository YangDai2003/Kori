package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.main.card.NoteItem
import org.yangdai.kori.presentation.component.main.card.NoteItemProperties

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Page(
    notes: List<NoteEntity>,
    noteItemProperties: NoteItemProperties,
    isSelectionMode: Boolean,
    selectedNotes: MutableSet<String>,
    keyword: String = "",
    navigateToNote: (String) -> Unit = { _ -> }
) = Box {

    val state = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()
    val showScrollToTopButton by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0 && state.lastScrolledBackward
        }
    }
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 256.dp),
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomPadding
        ),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(
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

    AnimatedVisibility(
        visible = showScrollToTopButton,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        OutlinedIconButton(
            modifier = Modifier.size(IconButtonDefaults.extraSmallContainerSize(widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform)),
            colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            onClick = {
                scope.launch {
                    state.animateScrollToItem(0)
                }
            }
        ) {
            Icon(
                Icons.Default.VerticalAlignTop,
                contentDescription = "Scroll to top",
                modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
            )
        }
    }
}