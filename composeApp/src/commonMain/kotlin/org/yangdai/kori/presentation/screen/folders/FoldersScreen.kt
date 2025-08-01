package org.yangdai.kori.presentation.screen.folders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.deleting_a_folder_will_also_delete_all_the_notes_it_contains_and_they_cannot_be_restored_do_you_want_to_continue
import kori.composeapp.generated.resources.folders
import kori.composeapp.generated.resources.note_count
import kori.composeapp.generated.resources.others
import kori.composeapp.generated.resources.sort_by
import kori.composeapp.generated.resources.starred
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.local.dao.FolderDao
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.defaultFolderColor
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBar
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.VerticalScrollbar
import org.yangdai.kori.presentation.component.dialog.FolderSortOptionBottomSheet
import org.yangdai.kori.presentation.component.dialog.ModifyFolderDialog
import org.yangdai.kori.presentation.component.dialog.WarningDialog
import org.yangdai.kori.presentation.component.rememberPlatformStyleTopAppBarState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(
    ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class, ExperimentalUuidApi::class
)
@Composable
fun FoldersScreen(
    viewModel: FoldersViewModel = koinViewModel(),
    navigateUp: () -> Unit
) {
    val groupedFolders by viewModel.foldersMap.collectAsStateWithLifecycle()
    var showSortDialog by rememberSaveable { mutableStateOf(false) }
    val topAppBarState = rememberPlatformStyleTopAppBarState()

    var selectedFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var deletingFolder by remember { mutableStateOf<FolderEntity?>(null) }
    val state = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    SharedTransitionLayout(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(topAppBarState.scrollBehavior.nestedScrollConnection),
            topBar = {
                PlatformStyleTopAppBar(
                    state = topAppBarState,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            scope.launch {
                                state.animateScrollToItem(0)
                            }
                        })
                    },
                    title = { PlatformStyleTopAppBarTitle(stringResource(Res.string.folders)) },
                    navigationIcon = { PlatformStyleTopAppBarNavigationIcon(onClick = navigateUp) },
                    actions = {
                        TooltipIconButton(
                            tipText = stringResource(Res.string.sort_by),
                            icon = Icons.Default.SortByAlpha,
                            onClick = { showSortDialog = true },
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    selectedFolder == null,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None
                ) {
                    FloatingActionButton(
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "-bounds"),
                            animatedVisibilityScope = this,
                            clipInOverlayDuringTransition = OverlayClip(FloatingActionButtonDefaults.shape)
                        ),
                        onClick = { selectedFolder = FolderEntity() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = null
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) { innerPadding ->

            val layoutDirection = LocalLayoutDirection.current

            Box(
                Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection)
                    )
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(320.dp),
                    state = state,
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!groupedFolders[true].isNullOrEmpty())
                        stickyHeader(key = "starred") {
                            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Text(
                                    modifier = Modifier.padding(bottom = 8.dp, start = 12.dp),
                                    text = stringResource(Res.string.starred),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                    items(groupedFolders[true] ?: emptyList(), key = { it.folder.id }) {
                        FolderItem(
                            modifier = Modifier.animateItem(),
                            folderWithNoteCount = it,
                            visible = selectedFolder != it.folder,
                            onClick = { selectedFolder = it.folder }
                        )
                    }

                    if (groupedFolders.size == 2)
                        stickyHeader(key = "others") {
                            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Text(
                                    modifier = Modifier.padding(bottom = 8.dp, start = 12.dp),
                                    text = stringResource(Res.string.others),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                    items(groupedFolders[false] ?: emptyList(), key = { it.folder.id }) {
                        FolderItem(
                            modifier = Modifier.animateItem(),
                            folderWithNoteCount = it,
                            visible = selectedFolder != it.folder,
                            onClick = { selectedFolder = it.folder }
                        )
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight(),
                    state = state
                )
            }
        }

        BackHandler(selectedFolder != null) {
            selectedFolder = null
        }

        ModifyFolderDialog(
            folder = selectedFolder,
            onDismissRequest = { selectedFolder = null },
            onDeleteRequest = {
                deletingFolder = selectedFolder
                selectedFolder = null
            },
            onConfirm = {
                if (it.id.isEmpty()) viewModel.createFolder(it.copy(id = Uuid.random().toString()))
                else viewModel.updateFolder(it)
                selectedFolder = null
            }
        )

        if (deletingFolder != null)
            WarningDialog(
                message = stringResource(Res.string.deleting_a_folder_will_also_delete_all_the_notes_it_contains_and_they_cannot_be_restored_do_you_want_to_continue),
                onDismissRequest = { deletingFolder = null },
                onConfirm = { viewModel.deleteFolder(deletingFolder!!) }
            )

        if (showSortDialog)
            FolderSortOptionBottomSheet(
                oFolderSortType = viewModel.folderSortType,
                onDismissRequest = { showSortDialog = false },
                onSortTypeSelected = { viewModel.setFolderSorting(it) }
            )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FolderItem(
    modifier: Modifier = Modifier,
    folderWithNoteCount: FolderDao.FolderWithNoteCount,
    visible: Boolean,
    onClick: () -> Unit
) = AnimatedVisibility(
    modifier = modifier,
    visible = visible,
    enter = fadeIn() + scaleIn(),
    exit = fadeOut() + scaleOut()
) {
    ElevatedCard(
        modifier = Modifier.padding(bottom = 8.dp)
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "${folderWithNoteCount.folder.id}-bounds"),
                animatedVisibilityScope = this,
                clipInOverlayDuringTransition = OverlayClip(CardDefaults.elevatedShape)
            ),
        onClick = onClick
    ) {
        val folderColor = if (folderWithNoteCount.folder.colorValue != defaultFolderColor)
            Color(folderWithNoteCount.folder.colorValue)
        else MaterialTheme.colorScheme.primary
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
                    text = folderWithNoteCount.folder.name,
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
                        folderWithNoteCount.noteCount,
                        folderWithNoteCount.noteCount
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}
