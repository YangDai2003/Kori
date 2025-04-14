package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.checked
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.delete_all
import kori.composeapp.generated.resources.move
import kori.composeapp.generated.resources.pin
import kori.composeapp.generated.resources.pinboard
import kori.composeapp.generated.resources.restore
import kori.composeapp.generated.resources.restore_all
import kori.composeapp.generated.resources.sort_by
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.trash
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.LazyGridScrollbar
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.dialog.NoteSortOptionDialog
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.viewModel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    viewModel: AppViewModel,
    currentDrawerItem: DrawerItem,
    navigationIcon: @Composable () -> Unit = {},
    navigateToScreen: (Screen) -> Unit
) {
    var showSortDialog by rememberSaveable { mutableStateOf(false) }
    var showFoldersDialog by rememberSaveable { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateSetOf<String>() }
    val isSelectionMode by remember(selectedNotes) {
        derivedStateOf {
            selectedNotes.isNotEmpty()
        }
    }

    Scaffold(
        topBar = {
            AnimatedContent(targetState = isSelectionMode) {
                if (it) {
                    // 多选模式的顶部操作栏
                    TopAppBar(
                        title = { PlatformStyleTopAppBarTitle("${stringResource(Res.string.checked)}${selectedNotes.size}") },
                        navigationIcon = {
                            IconButton(onClick = { selectedNotes.clear() }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        },
                        actions = {
                            if (currentDrawerItem !is DrawerItem.Trash && currentDrawerItem !is DrawerItem.Templates) {
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.pin),
                                    icon = painterResource(Res.drawable.pinboard),
                                    onClick = {
                                        selectedNotes.forEach { noteId ->
                                            viewModel.pinNote(noteId)
                                        }
                                        selectedNotes.clear()
                                    }
                                )
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.move),
                                    icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                                    onClick = { showFoldersDialog = true }
                                )
                            }

                            if (currentDrawerItem is DrawerItem.Trash) {
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.restore),
                                    icon = Icons.Outlined.RestartAlt,
                                    onClick = {
                                        selectedNotes.forEach { noteId ->
                                            viewModel.restoreNoteFromTrash(noteId)
                                        }
                                        selectedNotes.clear()
                                    }
                                )
                            }

                            TooltipIconButton(
                                tipText = stringResource(Res.string.delete),
                                icon = if (currentDrawerItem is DrawerItem.Trash) Icons.Outlined.DeleteForever
                                else Icons.Outlined.Delete,
                                onClick = {
                                    when (currentDrawerItem) {
                                        DrawerItem.Trash -> {
                                            // 从回收站永久删除
                                            selectedNotes.forEach { noteId ->
                                                viewModel.deleteNote(noteId)
                                            }
                                        }

                                        else -> {
                                            // 移到回收站
                                            selectedNotes.forEach { noteId ->
                                                viewModel.moveNoteToTrash(noteId)
                                            }
                                        }
                                    }
                                    selectedNotes.clear()
                                }
                            )
                        }
                    )
                } else {
                    // 常规模式的顶部应用栏
                    TopAppBar(
                        title = {
                            when (currentDrawerItem) {
                                DrawerItem.AllNotes -> PlatformStyleTopAppBarTitle(stringResource(Res.string.all_notes))
                                DrawerItem.Templates -> PlatformStyleTopAppBarTitle(stringResource(Res.string.templates))
                                DrawerItem.Trash -> PlatformStyleTopAppBarTitle(stringResource(Res.string.trash))
                                is DrawerItem.Folder -> PlatformStyleTopAppBarTitle(currentDrawerItem.folder.name)
                            }
                        },
                        actions = {
                            TooltipIconButton(
                                tipText = stringResource(Res.string.sort_by),
                                icon = Icons.Outlined.SortByAlpha,
                                onClick = { showSortDialog = true }
                            )

                            if (currentDrawerItem is DrawerItem.Trash) {
                                var showMenu by remember { mutableStateOf(false) }
                                IconButton(onClick = { showMenu = !showMenu }) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = null
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.RestartAlt,
                                                contentDescription = null
                                            )
                                        },
                                        text = { Text(text = stringResource(Res.string.restore_all)) },
                                        onClick = { viewModel.restoreAllNotesFromTrash() })

                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = null
                                            )
                                        },
                                        text = { Text(text = stringResource(Res.string.delete_all)) },
                                        onClick = { viewModel.emptyTrash() })
                                }
                            }
                        },
                        navigationIcon = navigationIcon
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentDrawerItem !is DrawerItem.Trash && !isSelectionMode)
                FloatingActionButton(
                    onClick = {
                        // 创建新笔记时不传递ID
                        navigateToScreen(Screen.Note())
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "创建笔记")
                }
        }
    ) { innerPadding ->
        val page = when (currentDrawerItem) {
            DrawerItem.AllNotes -> 0
            DrawerItem.Templates -> 1
            DrawerItem.Trash -> 2
            is DrawerItem.Folder -> 3
        }
        val pagerState = rememberPagerState { 4 }
        LaunchedEffect(page) {
            selectedNotes.clear()
            pagerState.scrollToPage(page)
        }
        val contentPadding = remember(innerPadding) {
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding()
            )
        }
        VerticalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            userScrollEnabled = false
        ) {
            when (it) {
                0 -> {
                    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
                    Page(
                        notes = notes,
                        contentPadding = contentPadding,
                        navigateToScreen = navigateToScreen,
                        selectedNotes = selectedNotes,
                        isSelectionMode = isSelectionMode
                    )
                }

                1 -> {
                    val notes by viewModel.templateNotes.collectAsStateWithLifecycle()
                    Page(
                        notes = notes,
                        contentPadding = contentPadding,
                        navigateToScreen = navigateToScreen,
                        selectedNotes = selectedNotes,
                        isSelectionMode = isSelectionMode
                    )
                }

                2 -> {
                    val notes by viewModel.trashNotes.collectAsStateWithLifecycle()
                    Page(
                        notes = notes,
                        contentPadding = contentPadding,
                        navigateToScreen = navigateToScreen,
                        selectedNotes = selectedNotes,
                        isSelectionMode = isSelectionMode
                    )
                }

                3 -> {
                    val notes by viewModel.currentFolderNotes.collectAsStateWithLifecycle()
                    if (currentDrawerItem is DrawerItem.Folder) {
                        val folderId = currentDrawerItem.folder.id
                        LaunchedEffect(folderId) {
                            viewModel.loadNotesByFolder(folderId)
                        }
                        Page(
                            notes = notes,
                            contentPadding = contentPadding,
                            navigateToScreen = navigateToScreen,
                            selectedNotes = selectedNotes,
                            isSelectionMode = isSelectionMode
                        )
                    }
                }
            }
        }
    }

    if (showSortDialog)
        NoteSortOptionDialog(
            initialNoteSortType = viewModel.noteSortType,
            onDismissRequest = { showSortDialog = false },
            onSortTypeSelected = {
                viewModel.setNoteSorting(it)
            }
        )

    if (showFoldersDialog) {
        val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsStateWithLifecycle()
        FoldersDialog(
            oFolderId = (currentDrawerItem as? DrawerItem.Folder)?.folder?.id,
            foldersWithNoteCounts = foldersWithNoteCounts,
            onDismissRequest = { showFoldersDialog = false },
            onSelect = { folderId ->
                showFoldersDialog = false
                selectedNotes.forEach {
                    viewModel.moveNoteToFolder(it, folderId)
                }
                selectedNotes.clear()
            }
        )
    }
}

@Composable
fun Page(
    notes: List<NoteEntity>,
    contentPadding: PaddingValues,
    navigateToScreen: (Screen) -> Unit,
    selectedNotes: MutableSet<String>,
    isSelectionMode: Boolean
) {
    // 显示笔记列表
    Box {
        val state = rememberLazyGridState()
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            columns = GridCells.Fixed(1),
            state = state
        ) {
            items(notes, key = { it.id }) { note ->
                NoteItem(
                    note = note,
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
        LazyGridScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            state = state
        )
    }
}
