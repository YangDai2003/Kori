package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yangdai.kori.presentation.viewModel.AppViewModel
import org.yangdai.kori.presentation.navigation.Screen

@Composable
fun MainScreenContent(
    viewModel: AppViewModel,
    innerPadding: PaddingValues,
    currentDrawerItem: DrawerItem,
    navigateToScreen: (Screen) -> Unit
) {
    val page = when (currentDrawerItem) {
        DrawerItem.AllNotes -> 0
        DrawerItem.Templates -> 1
        DrawerItem.Trash -> 2
        is DrawerItem.Folder -> 3
    }
    val pagerState = rememberPagerState { 4 }
    LaunchedEffect(page) {
        pagerState.scrollToPage(page)
    }

    VerticalPager(
        state = pagerState,
        userScrollEnabled = false
    ) {
        when (it) {
            0 -> AllNotesScreen(viewModel, innerPadding, navigateToScreen)
            1 -> {
                Column(Modifier.padding(innerPadding)) {
                    Text("模板", style = MaterialTheme.typography.titleLarge)
                }
            }

            2 -> {
                Column(Modifier.padding(innerPadding)) {
                    Text("回收站", style = MaterialTheme.typography.titleLarge)
                }
            }

            3 -> {
                if (currentDrawerItem is DrawerItem.Folder) {
                    val name = currentDrawerItem.folder.name
                    Column(Modifier.padding(innerPadding)) {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun AllNotesScreen(
    viewModel: AppViewModel,
    innerPadding: PaddingValues,
    navigateToScreen: (Screen) -> Unit
) {
    val notes by viewModel.activeNotes.collectAsState()
    if (notes.isEmpty()) {
        // 显示空状态
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "没有笔记，点击右下角按钮创建新笔记",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        // 显示笔记列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(notes) { note ->
                NoteItem(
                    note = note,
                    onClick = {
                        navigateToScreen(Screen.Note(note.id))
                    }
                )
            }
        }
    }
}