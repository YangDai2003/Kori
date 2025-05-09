package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.ExperimentalWindowCoreApi
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.add
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.checked
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.delete_all
import kori.composeapp.generated.resources.learn_more
import kori.composeapp.generated.resources.move
import kori.composeapp.generated.resources.pin
import kori.composeapp.generated.resources.pinboard
import kori.composeapp.generated.resources.restore
import kori.composeapp.generated.resources.restore_all
import kori.composeapp.generated.resources.sample_note
import kori.composeapp.generated.resources.search
import kori.composeapp.generated.resources.search_history
import kori.composeapp.generated.resources.sort_by
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.trash
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.domain.sort.NoteSortType
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.FilePickerDialog
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.dialog.NoteSortOptionDialog
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.theme.linkColor
import org.yangdai.kori.presentation.util.rememberIsScreenSizeLarge

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class,
    ExperimentalWindowCoreApi::class
)
@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    currentDrawerItem: DrawerItem,
    navigationIcon: @Composable () -> Unit = {},
    navigateToScreen: (Screen) -> Unit
) {
    var showSortDialog by rememberSaveable { mutableStateOf(false) }
    var showFoldersDialog by rememberSaveable { mutableStateOf(false) }
    var showFilePickerDialog by rememberSaveable { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateSetOf<String>() }
    val isSelectionMode by remember(selectedNotes) {
        derivedStateOf { selectedNotes.isNotEmpty() }
    }
    val isLargeScreen = rememberIsScreenSizeLarge()
    BackHandler(enabled = isSelectionMode) { selectedNotes.clear() }

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
                                    icon = Icons.Outlined.RestoreFromTrash,
                                    onClick = {
                                        viewModel.restoreNotesFromTrash(selectedNotes.toSet())
                                        selectedNotes.clear()
                                    }
                                )
                            }

                            val deleteForever =
                                currentDrawerItem is DrawerItem.Trash || currentDrawerItem is DrawerItem.Templates
                            TooltipIconButton(
                                tipText = stringResource(Res.string.delete),
                                icon = if (deleteForever) Icons.Outlined.DeleteForever
                                else Icons.Outlined.Delete,
                                colors = IconButtonDefaults.iconButtonColors()
                                    .copy(contentColor = if (deleteForever) MaterialTheme.colorScheme.error else LocalContentColor.current),
                                onClick = {
                                    if (deleteForever) // 永久删除
                                        viewModel.deleteNotes(selectedNotes.toSet())
                                    else
                                        viewModel.moveNotesToTrash(selectedNotes.toSet())
                                    selectedNotes.clear()
                                }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                            .copy(containerColor = Color.Transparent)
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
                                                    imageVector = Icons.Outlined.RestoreFromTrash,
                                                    contentDescription = null
                                                )
                                            },
                                            text = { Text(text = stringResource(Res.string.restore_all)) },
                                            onClick = { viewModel.restoreAllNotesFromTrash() })

                                        DropdownMenuItem(
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.DeleteForever,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    contentDescription = null
                                                )
                                            },
                                            text = {
                                                Text(
                                                    text = stringResource(Res.string.delete_all),
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            },
                                            onClick = { viewModel.emptyTrash() })
                                    }
                                }
                            },
                            navigationIcon = navigationIcon,
                            colors = TopAppBarDefaults.topAppBarColors()
                                .copy(containerColor = Color.Transparent)
                        )
                }
            }
        },
        floatingActionButton = {
            if (currentDrawerItem !is DrawerItem.Trash && !isSelectionMode) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    label = "scale"
                )

                Surface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .combinedClickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                if (currentDrawerItem is DrawerItem.Templates)
                                    navigateToScreen(Screen.Template())
                                else {
                                    val folderId =
                                        if (currentDrawerItem is DrawerItem.Folder) currentDrawerItem.folder.id
                                        else null
                                    navigateToScreen(Screen.Note(folderId = folderId))
                                }
                            },
                            onLongClick = {
                                showFilePickerDialog = true
                            }
                        ),
                    shape = FloatingActionButtonDefaults.shape,
                    color = FloatingActionButtonDefaults.containerColor,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier.defaultMinSize(56.dp, 56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.Create, contentDescription = "Add")
                    }
                }
            }
        },
        containerColor = if (isLargeScreen) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceContainerLow
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
        val columns by remember(size) {
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

        val cardPaneState by viewModel.cardPaneState.collectAsStateWithLifecycle()
        val noteItemProperties by remember(cardPaneState, viewModel.noteSortType) {
            derivedStateOf {
                NoteItemProperties(
                    showCreatedTime = when (viewModel.noteSortType) {
                        NoteSortType.CREATE_TIME_DESC, NoteSortType.CREATE_TIME_ASC -> true
                        else -> false
                    },
                    cardSize = cardPaneState.cardSize,
                    clipOverflow = cardPaneState.clipOverflow
                )
            }
        }
        VerticalPager(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .graphicsLayer {
                    shape =
                        if ((isLargeScreen && currentDrawerItem !is DrawerItem.AllNotes) || isSelectionMode)
                            RoundedCornerShape(topStart = 8.dp)
                        else RectangleShape
                    clip = true
                }
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .onSizeChanged { size = it },
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { it },
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
                                    .padding(top = 8.dp)
                                    .padding(horizontal = 16.dp),
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
                                top = 78.dp + statusBarPadding,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        }

                        if (allNotes.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValue),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val annotatedString = buildAnnotatedString {
                                    append(stringResource(Res.string.add) + " ")
                                    withLink(
                                        LinkAnnotation.Url(
                                            "",
                                            TextLinkStyles(
                                                style = SpanStyle(
                                                    color = linkColor,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        ) {
                                            viewModel.addSampleNote()
                                        }
                                    ) {
                                        append(stringResource(Res.string.sample_note))
                                    }
                                    append(" " + stringResource(Res.string.learn_more))
                                }
                                Text(
                                    text = annotatedString,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            AnimatedContent(inputText.isNotBlank() && !expanded) { showSearchRes ->
                                if (showSearchRes)
                                    SearchResultsPage(
                                        keyword = inputText,
                                        notes = searchResults,
                                        contentPadding = paddingValue,
                                        navigateToScreen = navigateToScreen,
                                        selectedNotes = selectedNotes,
                                        columns = columns,
                                        noteItemProperties = noteItemProperties,
                                        isSelectionMode = isSelectionMode
                                    )
                                else
                                    Page(
                                        notes = allNotes,
                                        contentPadding = paddingValue,
                                        navigateToScreen = navigateToScreen,
                                        selectedNotes = selectedNotes,
                                        columns = columns,
                                        noteItemProperties = noteItemProperties,
                                        isSelectionMode = isSelectionMode
                                    )
                            }
                        }
                    }
                }

                1 -> {
                    val templateNotes by viewModel.templateNotes.collectAsStateWithLifecycle()
                    TemplatesPage(
                        notes = templateNotes,
                        contentPadding = contentPadding,
                        navigateToScreen = navigateToScreen,
                        selectedNotes = selectedNotes,
                        columns = columns,
                        noteItemProperties = noteItemProperties,
                        isSelectionMode = isSelectionMode
                    )
                }

                2 -> {
                    val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()
                    Page(
                        notes = trashNotes,
                        contentPadding = contentPadding,
                        navigateToScreen = { },
                        selectedNotes = selectedNotes,
                        columns = columns,
                        noteItemProperties = noteItemProperties,
                        isSelectionMode = isSelectionMode
                    )
                }

                3 -> {
                    val folderNotesMap by viewModel.currentFolderNotesMap.collectAsStateWithLifecycle()
                    if (currentDrawerItem is DrawerItem.Folder) {
                        val folderId = currentDrawerItem.folder.id
                        LaunchedEffect(folderId) {
                            viewModel.loadNotesByFolder(folderId)
                        }
                        if (folderNotesMap.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val annotatedString = buildAnnotatedString {
                                    append(stringResource(Res.string.add) + " ")
                                    withLink(
                                        LinkAnnotation.Url(
                                            "",
                                            TextLinkStyles(
                                                style = SpanStyle(
                                                    color = linkColor,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        ) {
                                            viewModel.addSampleNote(folderId)
                                        }
                                    ) {
                                        append(stringResource(Res.string.sample_note))
                                    }
                                    append(" " + stringResource(Res.string.learn_more))
                                }
                                Text(
                                    text = annotatedString,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else
                            GroupedPage(
                                notesMap = folderNotesMap,
                                contentPadding = contentPadding,
                                navigateToScreen = navigateToScreen,
                                selectedNotes = selectedNotes,
                                columns = columns,
                                noteItemProperties = noteItemProperties,
                                isSelectionMode = isSelectionMode
                            )
                    }
                }
            }
        }
    }

    if (showSortDialog)
        NoteSortOptionDialog(
            oNoteSortType = viewModel.noteSortType,
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

    if (showFilePickerDialog)
        FilePickerDialog { pickedFile ->
            showFilePickerDialog = false
            pickedFile?.let {
                navigateToScreen(Screen.File)
            }
        }
}
