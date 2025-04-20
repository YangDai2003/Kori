package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.ExperimentalWindowCoreApi
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.checked
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.delete_all
import kori.composeapp.generated.resources.move
import kori.composeapp.generated.resources.others
import kori.composeapp.generated.resources.pin
import kori.composeapp.generated.resources.pinboard
import kori.composeapp.generated.resources.pinned
import kori.composeapp.generated.resources.restore
import kori.composeapp.generated.resources.restore_all
import kori.composeapp.generated.resources.search
import kori.composeapp.generated.resources.search_history
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
import org.yangdai.kori.presentation.util.rememberIsScreenSizeLarge
import org.yangdai.kori.presentation.viewModel.AppViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalWindowCoreApi::class
)
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
    val isLargeScreen = rememberIsScreenSizeLarge()
    BackHandler(enabled = isSelectionMode) {
        selectedNotes.clear()
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
                                        viewModel.pinNotes(selectedNotes.toSet())
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
                                        viewModel.restoreNotesFromTrash(selectedNotes.toSet())
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
                                            viewModel.deleteNotes(selectedNotes.toSet())
                                        }

                                        else -> {
                                            // 移到回收站
                                            viewModel.moveNotesToTrash(selectedNotes.toSet())
                                        }
                                    }
                                    selectedNotes.clear()
                                }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                            .copy(
                                containerColor =
                                    if (isLargeScreen) MaterialTheme.colorScheme.surfaceContainer
                                    else MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp)
                            )
                    )
                } else {
                    // 常规模式的顶部应用栏
                    if (currentDrawerItem !is DrawerItem.AllNotes)
                        TopAppBar(
                            title = {
                                when (currentDrawerItem) {
                                    DrawerItem.AllNotes ->
                                        PlatformStyleTopAppBarTitle(stringResource(Res.string.all_notes))

                                    DrawerItem.Templates ->
                                        PlatformStyleTopAppBarTitle(stringResource(Res.string.templates))

                                    DrawerItem.Trash ->
                                        PlatformStyleTopAppBarTitle(stringResource(Res.string.trash))

                                    is DrawerItem.Folder ->
                                        PlatformStyleTopAppBarTitle(currentDrawerItem.folder.name)
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
                            navigationIcon = navigationIcon,
                            colors = TopAppBarDefaults.topAppBarColors()
                                .copy(
                                    containerColor =
                                        if (isLargeScreen) MaterialTheme.colorScheme.surfaceContainer
                                        else MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp)
                                )
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
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
        val contentPadding by remember(innerPadding, isLargeScreen) {
            derivedStateOf {
                PaddingValues(
                    top = if (isLargeScreen) 16.dp else 0.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
            }
        }
        var size by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current
        val columns by remember(size, density) {
            derivedStateOf {
                val windowSizeClass: WindowSizeClass =
                    WindowSizeClass.compute(size.width, size.height, density.density)
                when (windowSizeClass.windowWidthSizeClass) {
                    WindowWidthSizeClass.COMPACT -> 1
                    WindowWidthSizeClass.MEDIUM -> 2
                    WindowWidthSizeClass.EXPANDED -> 3
                    else -> 1
                }
            }
        }

        VerticalPager(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .clip(if (isLargeScreen) RoundedCornerShape(topStart = 8.dp) else RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp))
                .onSizeChanged { size = it },
            state = pagerState,
            userScrollEnabled = false
        ) { pager ->
            when (pager) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        var expanded by remember { mutableStateOf(false) }
                        var inputText by remember { mutableStateOf("") }
                        val searchHistorySet by viewModel.searchHistorySet.collectAsStateWithLifecycle()
                        val searchBarShadowElevation by animateDpAsState(if (expanded) 8.dp else 0.dp)
                        if (!isSelectionMode)
                            DockedSearchBar(
                                modifier = Modifier.align(Alignment.TopCenter)
                                    .statusBarsPadding()
                                    .padding(8.dp),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = inputText,
                                        onQueryChange = { inputText = it },
                                        onSearch = {
                                            viewModel.searchNotes(inputText)
                                            expanded = false
                                        },
                                        expanded = expanded,
                                        onExpandedChange = { expanded = it },
                                        placeholder = { Text(text = stringResource(Res.string.search)) },
                                        leadingIcon = {
                                            AnimatedContent(isLargeScreen || expanded) { showSearchIcon ->
                                                if (showSearchIcon)
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.searchNotes(inputText)
                                                            expanded = false
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Search,
                                                            contentDescription = null
                                                        )
                                                    }
                                                else navigationIcon()
                                            }
                                        },
                                        trailingIcon = {
                                            AnimatedContent(expanded) { showClearIcon ->
                                                if (showClearIcon)
                                                    IconButton(
                                                        onClick = {
                                                            if (inputText.isNotEmpty())
                                                                inputText = ""
                                                            else {
                                                                expanded = false
                                                                viewModel.searchNotes("")
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = null
                                                        )
                                                    }
                                                else
                                                    TooltipIconButton(
                                                        tipText = stringResource(Res.string.sort_by),
                                                        icon = Icons.Outlined.SortByAlpha,
                                                        onClick = { showSortDialog = true }
                                                    )
                                            }
                                        }
                                    )
                                },
                                shadowElevation = searchBarShadowElevation,
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                if (searchHistorySet.isEmpty()) return@DockedSearchBar
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Outlined.History,
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = { Text(stringResource(Res.string.search_history)) },
                                    trailingContent = {
                                        Icon(
                                            modifier = Modifier.clickable {
                                                viewModel.clearSearchHistory()
                                            },
                                            imageVector = Icons.Outlined.DeleteForever,
                                            contentDescription = null
                                        )
                                    }
                                )
                                FlowRow(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    maxLines = 3
                                ) {
                                    searchHistorySet.reversed().forEach {
                                        SuggestionChip(
                                            modifier = Modifier.defaultMinSize(48.dp),
                                            onClick = { inputText = it },
                                            label = {
                                                Text(
                                                    text = it,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            })
                                    }
                                }
                            }

                        val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
                        val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                        val statusBarPadding =
                            WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        val paddingValue = if (isSelectionMode) {
                            PaddingValues(
                                top = 16.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        } else {
                            PaddingValues(
                                top = 74.dp + statusBarPadding,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        }

                        AnimatedContent(inputText.isNotBlank() && !expanded) { showSearchRes ->
                            if (showSearchRes)
                                Page(
                                    notes = searchResults,
                                    contentPadding = paddingValue,
                                    navigateToScreen = navigateToScreen,
                                    selectedNotes = selectedNotes,
                                    columns = columns,
                                    isSelectionMode = isSelectionMode
                                )
                            else
                                Page(
                                    notes = allNotes,
                                    contentPadding = paddingValue,
                                    navigateToScreen = navigateToScreen,
                                    selectedNotes = selectedNotes,
                                    columns = columns,
                                    isSelectionMode = isSelectionMode
                                )
                        }
                    }
                }

                1 -> {
                    val notes by viewModel.templateNotes.collectAsStateWithLifecycle()
                    Page(
                        notes = notes,
                        contentPadding = contentPadding,
                        navigateToScreen = navigateToScreen,
                        selectedNotes = selectedNotes,
                        columns = columns,
                        isSelectionMode = isSelectionMode
                    )
                }

                2 -> {
                    val notes by viewModel.trashNotes.collectAsStateWithLifecycle()
                    Page(
                        notes = notes,
                        contentPadding = contentPadding,
                        navigateToScreen = { },
                        selectedNotes = selectedNotes,
                        columns = columns,
                        isSelectionMode = isSelectionMode
                    )
                }

                3 -> {
                    val notesMap by viewModel.currentFolderNotesMap.collectAsStateWithLifecycle()
                    if (currentDrawerItem is DrawerItem.Folder) {
                        val folderId = currentDrawerItem.folder.id
                        LaunchedEffect(folderId) {
                            viewModel.loadNotesByFolder(folderId)
                        }
                        GroupedPage(
                            notesMap = notesMap,
                            contentPadding = contentPadding,
                            navigateToScreen = navigateToScreen,
                            selectedNotes = selectedNotes,
                            columns = columns,
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
                viewModel.moveNotesToFolder(selectedNotes.toSet(), folderId)
                selectedNotes.clear()
            }
        )
    }
}

@Composable
fun GroupedPage(
    notesMap: Map<Boolean, List<NoteEntity>>,
    contentPadding: PaddingValues,
    columns: Int,
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
            columns = GridCells.Fixed(columns),
            state = state,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!notesMap[true].isNullOrEmpty())
                stickyHeader(key = "pinned") {
                    Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp)) {
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
                    Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp)) {
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
}

@Composable
fun Page(
    notes: List<NoteEntity>,
    contentPadding: PaddingValues,
    columns: Int,
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
