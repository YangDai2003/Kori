package org.yangdai.kori.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.content
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.event.UiEvent
import org.yangdai.kori.presentation.util.rememberIsScreenSizeLarge
import org.yangdai.kori.presentation.viewModel.NoteViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel = koinViewModel<NoteViewModel>(),
    noteId: String,
    navigateUp: () -> Unit
) {

    val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsStateWithLifecycle()
    val noteEditingState by viewModel.noteEditingState.collectAsStateWithLifecycle()

    // 确保屏幕旋转等配置变更时，不会重复加载笔记
    var lastLoadedNoteId by rememberSaveable { mutableStateOf<String>("") }

    DisposableEffect(Unit) {
        if (noteId != lastLoadedNoteId) {
            lastLoadedNoteId = noteId
            viewModel.loadNoteById(noteId)
        }
        onDispose {
            viewModel.saveOrUpdateNote()
        }
    }
    LaunchedEffect(true) {
        viewModel.uiEventFlow.collect { event ->
            if (event is UiEvent.NavigateUp) navigateUp()
        }
    }

    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
    var folderName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(noteEditingState.folderId) {
        withContext(Dispatchers.Default) {
            folderName = if (noteEditingState.folderId == null) {
                getString(Res.string.all_notes)
            } else {
                foldersWithNoteCounts.find { it.folder.id == noteEditingState.folderId }?.folder?.name
                    ?: getString(Res.string.all_notes)
            }
        }
    }

    var isSearching by remember { mutableStateOf(false) }
    var findAndReplaceState by remember { mutableStateOf(FindAndReplaceState()) }
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    FilledTonalButton(
                        modifier = Modifier.sizeIn(maxWidth = 160.dp),
                        onClick = { showFolderDialog = true }
                    ) {
                        Text(
                            text = folderName, maxLines = 1, modifier = Modifier.basicMarquee()
                        )
                    }
                },
                navigationIcon = {
                    PlatformStyleTopAppBarNavigationIcon(onClick = navigateUp)
                },
                actions = {
                    // 置顶按钮
//                    TooltipIconButton(
//                        tipText = if (noteEditingState.isPinned) stringResource(Res.string.unpin) else stringResource(Res.string.pin),
//                        icon = if (noteEditingState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
//                        onClick = { viewModel.togglePinStatus() }
//                    )

                    TooltipIconButton(
                        tipText = "Find and Replace",
                        icon = Icons.Outlined.Search,
                        onClick = {
                            isSearching = !isSearching
                        }
                    )

                    // 删除按钮
                    TooltipIconButton(
                        tipText = stringResource(Res.string.delete),
                        icon = Icons.Outlined.Delete,
                        onClick = {
                            viewModel.moveNoteToTrash()
                        }
                    )

                    TooltipIconButton(
                        tipText = "Open drawer",
                        icon = painterResource(Res.drawable.right_panel_open),
                        onClick = {
                            isSideSheetOpen = true
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(isSearching) {
                if (it)
                    FindAndReplaceField(
                        state = findAndReplaceState,
                        onStateUpdate = { findAndReplaceState = it }
                    )
                else BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = viewModel.titleState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    decorator = { innerTextField ->
                        TextFieldDefaults.DecorationBox(
                            value = viewModel.titleState.text.toString(),
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember { MutableInteractionSource() },
                            placeholder = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(Res.string.title),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            },
                            contentPadding = PaddingValues(0.dp),
                            container = {}
                        )
                    }
                )
            }

            BasicTextField(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                state = viewModel.contentState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorator = { innerTextField ->
                    Box {
                        if (viewModel.contentState.text.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.content),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = HeaderNode(title = "test", level = 1, range = IntRange(0, 0)),
        onHeaderClick = {},
        navigateTo = {},
        actionContent = {},
        drawerContent = {}
    )

    if (showFolderDialog) {
        FoldersDialog(
            oFolderId = noteEditingState.folderId,
            foldersWithNoteCounts = foldersWithNoteCounts,
            onDismissRequest = { showFolderDialog = false },
            onSelect = { folderId ->
                showFolderDialog = false
                viewModel.moveNoteToFolder(folderId)
            }
        )
    }
}
