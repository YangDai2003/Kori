package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.delete_all
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.duplicate
import kori.composeapp.generated.resources.duplicate_24px
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.move
import kori.composeapp.generated.resources.pin
import kori.composeapp.generated.resources.pinboard
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.restore
import kori.composeapp.generated.resources.restore_all
import kori.composeapp.generated.resources.search
import kori.composeapp.generated.resources.search_history
import kori.composeapp.generated.resources.snackbar_deleted_all
import kori.composeapp.generated.resources.snackbar_deleted_forever
import kori.composeapp.generated.resources.snackbar_moved_to_trash
import kori.composeapp.generated.resources.snackbar_pinned
import kori.composeapp.generated.resources.snackbar_restored
import kori.composeapp.generated.resources.snackbar_restored_all
import kori.composeapp.generated.resources.sort_by
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.todo_text
import kori.composeapp.generated.resources.toolbox
import kori.composeapp.generated.resources.total
import kori.composeapp.generated.resources.trash
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.domain.sort.NoteSortType
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.dialog.NoteSortOptionBottomSheet
import org.yangdai.kori.presentation.component.main.card.NoteItemProperties
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.util.isScreenWidthExpanded

data class Digit(val digitChar: Char, val fullNumber: Int, val place: Int) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Digit -> digitChar == other.digitChar
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = digitChar.hashCode()
        result = 31 * result + fullNumber
        result = 31 * result + place
        return result
    }
}

operator fun Digit.compareTo(other: Digit): Int {
    return fullNumber.compareTo(other.fullNumber)
}

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    currentDrawerItem: DrawerItem,
    navigationIcon: @Composable () -> Unit,
    navigateToScreen: (Screen) -> Unit
) {
    var showSortDialog by remember { mutableStateOf(false) }
    var showFoldersDialog by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateSetOf<String>() }
    val isSelectionMode by remember { derivedStateOf { selectedNotes.isNotEmpty() } }
    val isLargeScreen = isScreenWidthExpanded()
    BackHandler(enabled = isSelectionMode) { selectedNotes.clear() }
    var fabMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(isSelectionMode) { if (isSelectionMode) fabMenuExpanded = false }
    BackHandler(enabled = fabMenuExpanded) { fabMenuExpanded = false }

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            colors = inputFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            ),
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = {
                scope.launch { searchBarState.animateToCollapsed() }
                viewModel.searchNotes(textFieldState.text.toString())
            },
            placeholder = { Text(stringResource(Res.string.search)) },
            leadingIcon = {
                AnimatedContent(isLargeScreen || searchBarState.currentValue == SearchBarValue.Expanded) { showSearchIcon ->
                    if (showSearchIcon)
                        IconButton(
                            colors = IconButtonDefaults.iconButtonVibrantColors(),
                            onClick = {
                                scope.launch { searchBarState.animateToCollapsed() }
                                viewModel.searchNotes(textFieldState.text.toString())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    else navigationIcon()
                }
            },
            trailingIcon = {
                AnimatedContent(searchBarState.currentValue == SearchBarValue.Expanded) { showClearIcon ->
                    if (showClearIcon)
                        IconButton(
                            onClick = {
                                if (textFieldState.text.isNotEmpty())
                                    textFieldState.clearText()
                                else {
                                    scope.launch { searchBarState.animateToCollapsed() }
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
                            icon = Icons.Default.SortByAlpha,
                            onClick = { showSortDialog = true }
                        )
                }
            }
        )
    }
    val cardPaneState by viewModel.cardPaneState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AnimatedContent(targetState = isSelectionMode) { isSelecting ->
                if (isSelecting) {
                    // 多选模式的顶部操作栏
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.animateContentSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                selectedNotes.size.toString()
                                    .mapIndexed { index, c -> Digit(c, selectedNotes.size, index) }
                                    .forEach { digit ->
                                        AnimatedContent(
                                            targetState = digit,
                                            transitionSpec = {
                                                if (targetState > initialState) {
                                                    slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                                } else {
                                                    slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                                                }
                                            }
                                        ) { digit ->
                                            Text(
                                                "${digit.digitChar}",
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                onClick = { selectedNotes.clear() }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        },
                        actions = {
                            if (currentDrawerItem is DrawerItem.AllNotes || currentDrawerItem is DrawerItem.Folder) {
                                val pinnedStr =
                                    pluralStringResource(
                                        Res.plurals.snackbar_pinned,
                                        selectedNotes.size,
                                        selectedNotes.size
                                    )
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.pin),
                                    icon = painterResource(Res.drawable.pinboard),
                                    onClick = {
                                        viewModel.pinNotes(selectedNotes.toSet())
                                        scope.launch { snackbarHostState.showSnackbar(pinnedStr) }
                                        selectedNotes.clear()
                                    }
                                )
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.move),
                                    icon = Icons.AutoMirrored.Outlined.DriveFileMove,
                                    onClick = { showFoldersDialog = true }
                                )
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.duplicate),
                                    icon = painterResource(Res.drawable.duplicate_24px),
                                    onClick = {
                                        viewModel.duplicateNotes(selectedNotes.toSet())
                                        selectedNotes.clear()
                                    }
                                )
                            }

                            if (currentDrawerItem is DrawerItem.Trash) {
                                val restoredStr =
                                    pluralStringResource(
                                        Res.plurals.snackbar_restored,
                                        selectedNotes.size,
                                        selectedNotes.size
                                    )
                                TooltipIconButton(
                                    tipText = stringResource(Res.string.restore),
                                    icon = Icons.Default.RestoreFromTrash,
                                    onClick = {
                                        viewModel.restoreNotesFromTrash(selectedNotes.toSet())
                                        scope.launch { snackbarHostState.showSnackbar(restoredStr) }
                                        selectedNotes.clear()
                                    }
                                )
                            }

                            val deleteForever =
                                currentDrawerItem is DrawerItem.Trash || currentDrawerItem is DrawerItem.Templates
                            val deletedForeverStr = pluralStringResource(
                                Res.plurals.snackbar_deleted_forever,
                                selectedNotes.size,
                                selectedNotes.size
                            )
                            val movedToTrashStr = pluralStringResource(
                                Res.plurals.snackbar_moved_to_trash,
                                selectedNotes.size,
                                selectedNotes.size
                            )
                            TooltipIconButton(
                                tipText = stringResource(Res.string.delete),
                                icon = if (deleteForever) Icons.Default.DeleteForever
                                else Icons.Outlined.Delete,
                                colors = IconButtonDefaults.iconButtonColors()
                                    .copy(contentColor = if (deleteForever) MaterialTheme.colorScheme.error else LocalContentColor.current),
                                onClick = {
                                    if (deleteForever) {
                                        viewModel.deleteNotes(selectedNotes.toSet())
                                        scope.launch {
                                            snackbarHostState.showSnackbar(deletedForeverStr)
                                        }
                                    } else {
                                        viewModel.moveNotesToTrash(selectedNotes.toSet())
                                        scope.launch {
                                            snackbarHostState.showSnackbar(movedToTrashStr)
                                        }
                                    }
                                    selectedNotes.clear()
                                }
                            )
                        },
                        expandedHeight = 56.dp,
                        windowInsets = if (isLargeScreen)
                            TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.End)
                        else TopAppBarDefaults.windowInsets,
                        colors = TopAppBarDefaults.topAppBarColors()
                            .copy(containerColor = Color.Transparent)
                    )
                } else {
                    // 常规模式的顶部应用栏
                    if (currentDrawerItem is DrawerItem.AllNotes) {
                        val searchHistorySet by viewModel.searchHistorySet.collectAsStateWithLifecycle()
                        AdaptiveSearchBar(
                            isLargeScreen = isLargeScreen,
                            searchBarState = searchBarState,
                            inputField = inputField
                        ) {
                            if (searchHistorySet.isEmpty()) return@AdaptiveSearchBar
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        stringResource(Res.string.search_history),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
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
                                searchHistorySet.reversed().forEach { it ->
                                    SuggestionChip(
                                        modifier = Modifier.defaultMinSize(48.dp),
                                        onClick = { textFieldState.setTextAndPlaceCursorAtEnd(it) },
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
                    } else
                        TopAppBar(
                            title = {
                                PlatformStyleTopAppBarTitle(
                                    when (currentDrawerItem) {
                                        DrawerItem.AllNotes -> stringResource(Res.string.all_notes)
                                        DrawerItem.Templates -> stringResource(Res.string.templates)
                                        DrawerItem.Trash -> stringResource(Res.string.trash)
                                        is DrawerItem.Folder -> currentDrawerItem.folder.name
                                        DrawerItem.Toolbox -> stringResource(Res.string.toolbox)
                                    }
                                )
                            },
                            subtitle = {
                                when (currentDrawerItem) {
                                    DrawerItem.Trash -> {
                                        val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()
                                        if (trashNotes.second.isNotEmpty())
                                            Text(text = "${stringResource(Res.string.total)}: ${trashNotes.second}")
                                    }

                                    else -> {}
                                }
                            },
                            actions = {
                                if (currentDrawerItem !is DrawerItem.Toolbox)
                                    TooltipIconButton(
                                        tipText = stringResource(Res.string.sort_by),
                                        icon = Icons.Default.SortByAlpha,
                                        onClick = { showSortDialog = true }
                                    )

                                if (currentDrawerItem is DrawerItem.Trash) {
                                    Box {
                                        var showMenu by remember { mutableStateOf(false) }
                                        IconButton(
                                            modifier =
                                                Modifier.minimumInteractiveComponentSize()
                                                    .size(
                                                        IconButtonDefaults.smallContainerSize(
                                                            IconButtonDefaults.IconButtonWidthOption.Narrow
                                                        )
                                                    ),
                                            shape = IconButtonDefaults.smallRoundShape,
                                            onClick = { showMenu = !showMenu }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.MoreVert,
                                                contentDescription = null
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            val deletedAllStr =
                                                stringResource(Res.string.snackbar_deleted_all)
                                            val restoredAllStr =
                                                stringResource(Res.string.snackbar_restored_all)
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.RestoreFromTrash,
                                                        contentDescription = null
                                                    )
                                                },
                                                text = { Text(text = stringResource(Res.string.restore_all)) },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.restoreAllNotesFromTrash()
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            restoredAllStr
                                                        )
                                                    }
                                                })

                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteForever,
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
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.emptyTrash()
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(deletedAllStr)
                                                    }
                                                })
                                        }
                                    }
                                }
                            },
                            expandedHeight = 56.dp,
                            navigationIcon = navigationIcon,
                            windowInsets = if (isLargeScreen)
                                TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.End)
                            else TopAppBarDefaults.windowInsets,
                            colors = TopAppBarDefaults.topAppBarColors()
                                .copy(containerColor = Color.Transparent)
                        )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        val pagerState = rememberPagerState { 5 }
        LaunchedEffect(currentDrawerItem) {
            selectedNotes.clear()
            fabMenuExpanded = false
            val page = when (currentDrawerItem) {
                DrawerItem.AllNotes -> 0
                DrawerItem.Templates -> 1
                DrawerItem.Trash -> 2
                is DrawerItem.Folder -> 3
                DrawerItem.Toolbox -> 4
            }
            pagerState.scrollToPage(page)
        }
        val contentPadding = remember(innerPadding) {
            PaddingValues(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding()
            )
        }
        val noteItemProperties = remember(cardPaneState, viewModel.noteSortType) {
            NoteItemProperties(
                showCreatedTime = when (viewModel.noteSortType) {
                    NoteSortType.CREATE_TIME_DESC, NoteSortType.CREATE_TIME_ASC -> true
                    else -> false
                },
                cardSize = cardPaneState.cardSize,
                clipOverflow = cardPaneState.clipOverflow
            )
        }
        Box(Modifier.fillMaxSize()) {
            VerticalPager(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = if (isLargeScreen) RoundedCornerShape(topStart = 12.dp)
                        else RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ),
                state = pagerState,
                beyondViewportPageCount = 1,
                key = { it },
                userScrollEnabled = false
            ) { pager ->
                when (pager) {
                    0 -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
                            val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                            AnimatedContent(textFieldState.text.isNotBlank() && searchBarState.currentValue == SearchBarValue.Collapsed) { showSearchRes ->
                                if (showSearchRes)
                                    Page(
                                        keyword = textFieldState.text.toString(),
                                        notes = searchResults,
                                        contentPadding = contentPadding,
                                        navigateToNote = { navigateToScreen(Screen.Note(it)) },
                                        selectedNotes = selectedNotes,
                                        noteItemProperties = noteItemProperties,
                                        isSelectionMode = isSelectionMode
                                    )
                                else
                                    Page(
                                        notes = allNotes,
                                        contentPadding = contentPadding,
                                        navigateToNote = { navigateToScreen(Screen.Note(it)) },
                                        selectedNotes = selectedNotes,
                                        noteItemProperties = noteItemProperties,
                                        isSelectionMode = isSelectionMode
                                    )
                            }
                        }
                    }

                    1 -> {
                        val templateNotes by viewModel.templateNotes.collectAsStateWithLifecycle()
                        Page(
                            notes = templateNotes,
                            contentPadding = contentPadding,
                            navigateToNote = { navigateToScreen(Screen.Template(it)) },
                            selectedNotes = selectedNotes,
                            noteItemProperties = noteItemProperties,
                            isSelectionMode = isSelectionMode
                        )
                    }

                    2 -> {
                        val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()
                        Page(
                            notes = trashNotes.first,
                            contentPadding = contentPadding,
                            selectedNotes = selectedNotes,
                            noteItemProperties = noteItemProperties,
                            isSelectionMode = isSelectionMode
                        )
                    }

                    3 -> {
                        val folderNotes by viewModel.currentFolderNotes.collectAsStateWithLifecycle()
                        if (currentDrawerItem is DrawerItem.Folder) {
                            val folderId = currentDrawerItem.folder.id
                            LaunchedEffect(folderId) {
                                viewModel.loadNotesByFolder(folderId)
                            }
                            Page(
                                notes = folderNotes,
                                contentPadding = contentPadding,
                                navigateToNote = { navigateToScreen(Screen.Note(it)) },
                                selectedNotes = selectedNotes,
                                noteItemProperties = noteItemProperties,
                                isSelectionMode = isSelectionMode
                            )
                        }
                    }

                    4 -> {
                        val scope = rememberCoroutineScope()
                        ToolboxPage(navigateToScreen) {
                            scope.launch {
                                val id = viewModel.addSampleNote(it)
                                navigateToScreen(Screen.Note(id))
                            }
                        }
                    }
                }
            }

            val commonItems = listOf(
                stringResource(Res.string.plain_text),
                stringResource(Res.string.markdown),
                stringResource(Res.string.todo_text)
            )

            val items = if (currentDrawerItem is DrawerItem.Templates) {
                commonItems
            } else {
                commonItems + stringResource(Res.string.drawing)
            }

            FloatingActionButtonMenu(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier.semantics {
                            traversalIndex = -1f
                            stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                            contentDescription = "Toggle menu"
                        }.animateFloatingActionButton(
                            visible = !(currentDrawerItem is DrawerItem.Trash || currentDrawerItem is DrawerItem.Toolbox || isSelectionMode),
                            alignment = Alignment.BottomEnd
                        ),
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = it }
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Outlined.Create
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                items.forEachIndexed { i, item ->
                    FloatingActionButtonMenuItem(
                        modifier = Modifier.semantics {
                            isTraversalGroup = true
                            // Add a custom a11y action to allow closing the menu when focusing
                            // the last menu item, since the close button comes before the first
                            // menu item in the traversal order.
                            if (i == items.size - 1) {
                                customActions =
                                    listOf(
                                        CustomAccessibilityAction(
                                            label = "Close menu",
                                            action = {
                                                fabMenuExpanded = false
                                                true
                                            }
                                        )
                                    )
                            }
                        },
                        onClick = {
                            fabMenuExpanded = false
                            if (currentDrawerItem is DrawerItem.Templates)
                                navigateToScreen(Screen.Template(noteType = i))
                            else {
                                val folderId =
                                    if (currentDrawerItem is DrawerItem.Folder) currentDrawerItem.folder.id
                                    else null
                                navigateToScreen(Screen.Note(folderId = folderId, noteType = i))
                            }
                        },
                        icon = { },
                        text = { Text(item) }
                    )
                }
            }
        }
    }

    if (showSortDialog)
        NoteSortOptionBottomSheet(
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
}
