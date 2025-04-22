package org.yangdai.kori.presentation.screen.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.deleting_a_folder_will_also_delete_all_the_notes_it_contains_and_they_cannot_be_restored_do_you_want_to_continue
import kori.composeapp.generated.resources.folders
import kori.composeapp.generated.resources.modify
import kori.composeapp.generated.resources.note_count
import kori.composeapp.generated.resources.others
import kori.composeapp.generated.resources.sort_by
import kori.composeapp.generated.resources.starred
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.Platform
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.presentation.component.LazyGridScrollbar
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBar
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.FolderSortOptionDialog
import org.yangdai.kori.presentation.component.dialog.ModifyFolderDialog
import org.yangdai.kori.presentation.component.dialog.WarningDialog
import org.yangdai.kori.presentation.util.rememberCurrentPlatform
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun FoldersScreen(
    viewModel: FoldersViewModel = koinViewModel(),
    navigateUp: () -> Unit
) {
    val groupedFolders by viewModel.foldersMap.collectAsStateWithLifecycle()
    var showAddFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showSortDialog by rememberSaveable { mutableStateOf(false) }
    val platform = rememberCurrentPlatform()
    val scrollBehavior = if (platform is Platform.Desktop) TopAppBarDefaults.pinnedScrollBehavior()
    else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var selectedFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var showModifyDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PlatformStyleTopAppBar(
                title = { PlatformStyleTopAppBarTitle(stringResource(Res.string.folders)) },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(onClick = navigateUp) },
                actions = {
                    TooltipIconButton(
                        tipText = stringResource(Res.string.sort_by),
                        icon = Icons.Outlined.SortByAlpha,
                        onClick = { showSortDialog = true },
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(scrolledContainerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddFolderDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.CreateNewFolder,
                    contentDescription = null
                )
            }
        }
    ) { innerPadding ->
        val state = rememberLazyGridState()
        val layoutDirection = LocalLayoutDirection.current
        Box(
            Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(380.dp),
                state = state,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!groupedFolders[true].isNullOrEmpty())
                    stickyHeader(key = "starred") {
                        Surface {
                            Text(
                                modifier = Modifier.padding(bottom = 8.dp, start = 12.dp),
                                text = stringResource(Res.string.starred),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                items(groupedFolders[true] ?: emptyList(), key = { it.folder.id }) {
                    FolderItem(
                        folder = it.folder,
                        notesCountInFolder = it.noteCount,
                        onModifyRequest = { folder ->
                            selectedFolder = folder
                            showModifyDialog = true
                        },
                        onDeleteRequest = { folder ->
                            selectedFolder = folder
                            showWarningDialog = true
                        }
                    )
                }

                if (groupedFolders.size == 2)
                    stickyHeader(key = "others") {
                        Surface {
                            Text(
                                modifier = Modifier.padding(bottom = 8.dp, start = 12.dp),
                                text = stringResource(Res.string.others),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                items(groupedFolders[false] ?: emptyList(), key = { it.folder.id }) {
                    FolderItem(
                        folder = it.folder,
                        notesCountInFolder = it.noteCount,
                        onModifyRequest = { folder ->
                            selectedFolder = folder
                            showModifyDialog = true
                        },
                        onDeleteRequest = { folder ->
                            selectedFolder = folder
                            showWarningDialog = true
                        }
                    )
                }
            }

            LazyGridScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                state = state
            )
        }

        if (showAddFolderDialog) {
            ModifyFolderDialog(
                folder = FolderEntity(Uuid.random().toString()),
                onDismissRequest = { showAddFolderDialog = false }
            ) { viewModel.createFolder(it) }
        }
    }

    if (showWarningDialog && selectedFolder != null) {
        WarningDialog(
            message = stringResource(Res.string.deleting_a_folder_will_also_delete_all_the_notes_it_contains_and_they_cannot_be_restored_do_you_want_to_continue),
            onDismissRequest = { showWarningDialog = false },
            onConfirm = {
                viewModel.deleteFolder(selectedFolder!!)
                showWarningDialog = false
            }
        )
    }

    if (showModifyDialog && selectedFolder != null) {
        ModifyFolderDialog(
            folder = selectedFolder!!,
            onDismissRequest = { showModifyDialog = false }
        ) {
            viewModel.updateFolder(it)
            showModifyDialog = false
        }
    }

    if (showSortDialog)
        FolderSortOptionDialog(
            initialFolderSortType = viewModel.folderSortType,
            onDismissRequest = { showSortDialog = false },
            onSortTypeSelected = { viewModel.setFolderSorting(it) }
        )
}

@Composable
fun LazyGridItemScope.FolderItem(
    folder: FolderEntity,
    notesCountInFolder: Int,
    onModifyRequest: (FolderEntity) -> Unit,
    onDeleteRequest: (FolderEntity) -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    val folderColor by remember(folder.colorValue, colorScheme) {
        derivedStateOf {
            if (folder.colorValue != FolderEntity.defaultColorValue)
                Color(folder.colorValue)
            else
                colorScheme.primary
        }
    }

    val onConfirmValueChange = remember(folder) {
        { value: SwipeToDismissBoxValue ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onModifyRequest(folder)
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteRequest(folder)
                    false
                }

                SwipeToDismissBoxValue.Settled -> true
            }
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = onConfirmValueChange
    )
    var showContextMenu by remember { mutableStateOf(false) }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.targetValue
            val progress = dismissState.progress
            val iconOffset = 30.dp * (progress * 1.5f)
            val backgroundColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> folderColor.copy(alpha = 0.1f)
                SwipeToDismissBoxValue.EndToStart -> colorScheme.errorContainer
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }

            val cornerLeftRadius =
                if (direction == SwipeToDismissBoxValue.StartToEnd) 16.dp * (progress * 6f) else 0.dp
            val cornerRightRadius =
                if (direction == SwipeToDismissBoxValue.EndToStart) 16.dp * (progress * 6f) else 0.dp
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = cornerLeftRadius,
                            topEnd = cornerRightRadius,
                            bottomStart = cornerLeftRadius,
                            bottomEnd = cornerRightRadius
                        )
                    )
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp)
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd)
                    Icon(
                        imageVector = Icons.Outlined.DriveFileRenameOutline,
                        contentDescription = null,
                        tint = folderColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(30.dp)
                            .offset { IntOffset(x = iconOffset.roundToPx(), y = 0) }
                    )
                if (direction == SwipeToDismissBoxValue.EndToStart)
                    Icon(
                        imageVector = Icons.Outlined.FolderDelete,
                        contentDescription = null,
                        tint = colorScheme.onErrorContainer,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(30.dp)
                            .offset { IntOffset(x = -iconOffset.roundToPx(), y = 0) }
                    )
            }
        },
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(CardDefaults.elevatedShape)
            .animateItem()
    ) {
        ElevatedCard(onClick = { showContextMenu = true }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(folderColor.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = folderColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = folderColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pluralStringResource(
                            Res.plurals.note_count,
                            notesCountInFolder,
                            notesCountInFolder
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        )
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.modify)) },
                leadingIcon = {
                    Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null)
                },
                onClick = {
                    onModifyRequest(folder)
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.delete)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                },
                onClick = {
                    onDeleteRequest(folder)
                    showContextMenu = false
                }
            )
        }
    }
}
