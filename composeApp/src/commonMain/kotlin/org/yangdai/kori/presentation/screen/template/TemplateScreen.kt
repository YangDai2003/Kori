package org.yangdai.kori.presentation.screen.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHorizontalCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.created
import kori.composeapp.generated.resources.find
import kori.composeapp.generated.resources.replace
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.side_sheet
import kori.composeapp.generated.resources.updated
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.SingleRowTopAppBar
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.ExportDialog
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.dialog.ShareDialog
import org.yangdai.kori.presentation.component.note.AIAssist
import org.yangdai.kori.presentation.component.note.AdaptiveActionRow
import org.yangdai.kori.presentation.component.note.AdaptiveEditor
import org.yangdai.kori.presentation.component.note.AdaptiveEditorViewer
import org.yangdai.kori.presentation.component.note.AdaptiveViewer
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.TitleText
import org.yangdai.kori.presentation.component.note.TitleTextField
import org.yangdai.kori.presentation.component.note.rememberFindAndReplaceState
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.util.formatInstant
import org.yangdai.kori.presentation.util.rememberIsScreenWidthExpanded
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel = koinViewModel(),
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {
    val editingState by viewModel.editingState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val showAI by viewModel.showAI.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveOrUpdateNote()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEventFlow.collect { event ->
            if (event is UiEvent.NavigateUp) navigateUp()
        }
    }

    var isReadView by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(editorState.isDefaultReadingView) {
        if (editorState.isDefaultReadingView && editingState.id.isNotEmpty()) {
            isReadView = true
        }
    }

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var editingTitle by remember { mutableStateOf(false) }
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    val findAndReplaceState = rememberFindAndReplaceState()
    var showNoteTypeDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val printTrigger = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(isReadView) {
        focusManager.clearFocus()
        isSearching = false
        pagerState.animateScrollToPage(if (isReadView) 1 else 0)
    }

    Scaffold(
        modifier = Modifier.imePadding().onPreviewKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                when (keyEvent.key) {
                    Key.F -> {
                        isSearching = !isSearching
                        true
                    }

                    Key.Tab -> {
                        isSideSheetOpen = !isSideSheetOpen
                        true
                    }

                    else -> false
                }
            } else false
        },
        topBar = {
            SingleRowTopAppBar(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    )
                },
                title = {
                    TitleText(
                        state = viewModel.titleState,
                        visible = !editingTitle,
                        onClick = { editingTitle = true }
                    )
                },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(navigateUp) },
                actions = {
                    if (!isReadView)
                        TooltipIconButton(
                            hint = "${stringResource(Res.string.find)} & ${stringResource(Res.string.replace)}",
                            actionText = "F",
                            icon = if (isSearching) Icons.Default.SearchOff
                            else Icons.Default.Search,
                            onClick = { isSearching = !isSearching }
                        )

                    TooltipIconButton(
                        icon = if (isReadView) Icons.Default.EditNote
                        else Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { isReadView = !isReadView }
                    )

                    TooltipIconButton(
                        hint = stringResource(Res.string.side_sheet),
                        actionText = "↹",
                        icon = painterResource(Res.drawable.right_panel_open),
                        onClick = { isSideSheetOpen = true }
                    )
                }
            )
        },
        bottomBar = {
            AdaptiveActionRow(
                visible = !isReadView && !isSearching,
                type = editingState.noteType,
                scrollState = scrollState,
                startPadding = if (showAI && editingState.noteType != NoteType.PLAIN_TEXT) 52.dp else 0.dp,
                textFieldState = viewModel.contentState,
                isTemplate = true
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            AnimatedVisibility(editingTitle) {
                TitleTextField(
                    state = viewModel.titleState,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    initFocus = true,
                    onDone = { editingTitle = false }
                )
            }

            AnimatedVisibility(isSearching) {
                FindAndReplaceField(findAndReplaceState)
            }

            if (editingState.noteType != NoteType.Drawing) {
                var firstVisibleCharPosition by remember { mutableIntStateOf(0) }
                AdaptiveEditorViewer(
                    showDualPane = rememberIsScreenWidthExpanded(),
                    pagerState = pagerState,
                    editor = { modifier ->
                        AdaptiveEditor(
                            modifier = modifier,
                            noteType = editingState.noteType,
                            textFieldState = viewModel.contentState,
                            scrollState = scrollState,
                            readOnly = isReadView,
                            isLineNumberVisible = editorState.isLineNumberVisible,
                            isLintingEnabled = editorState.isLintingEnabled,
                            headerRange = selectedHeader,
                            findAndReplaceState = findAndReplaceState,
                            onScroll = { firstVisibleCharPosition = it }
                        )
                    },
                    viewer = if (editingState.noteType == NoteType.MARKDOWN || editingState.noteType == NoteType.TODO) { modifier ->
                        AdaptiveViewer(
                            modifier = modifier,
                            noteType = editingState.noteType,
                            textFieldState = viewModel.contentState,
                            firstVisibleCharPosition = firstVisibleCharPosition,
                            isSheetVisible = isSideSheetOpen,
                            printTrigger = printTrigger
                        )
                    } else null
                )
            }
        }
    }

    AnimatedVisibility(
        visible = showAI && !isReadView && !isSearching,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AIAssist(
            isGenerating = isGenerating,
            isTextSelectionCollapsed = viewModel.contentState.selection.collapsed,
        ) { viewModel.onAIAssistEvent(it) }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        text = viewModel.contentState.text.toString(),
        noteType = editingState.noteType,
        onHeaderClick = { selectedHeader = it },
        navigateTo = { navigateToScreen(it) },
        actionContent = {
            IconButton(onClick = { showNoteTypeDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.SwapHorizontalCircle,
                    contentDescription = null
                )
            }

            IconButton(onClick = { viewModel.deleteTemplate() }) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = null
                )
            }

            if (!currentPlatformInfo.isDesktop())
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null
                    )
                }

            IconButton(onClick = { showExportDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.FileUpload,
                    contentDescription = null
                )
            }

            AnimatedVisibility(editingState.noteType == NoteType.MARKDOWN) {
                IconButton(onClick = { printTrigger.value = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Print,
                        contentDescription = null
                    )
                }
            }
        },
        drawerContent = {
            val formattedCreated = remember(editingState.createdAt) {
                if (editingState.createdAt.isBlank()) ""
                else formatInstant(Instant.parse(editingState.createdAt))
            }
            NoteSideSheetItem(
                key = stringResource(Res.string.created),
                value = formattedCreated
            )
            val formattedUpdated = remember(editingState.updatedAt) {
                if (editingState.updatedAt.isBlank()) ""
                else formatInstant(Instant.parse(editingState.updatedAt))
            }
            NoteSideSheetItem(
                key = stringResource(Res.string.updated),
                value = formattedUpdated
            )
        }
    )

    if (showNoteTypeDialog) {
        NoteTypeDialog(
            oNoteType = editingState.noteType,
            onDismissRequest = { showNoteTypeDialog = false },
            onNoteTypeSelected = { noteType ->
                showNoteTypeDialog = false
                viewModel.updateNoteType(noteType)
            }
        )
    }

    if (showShareDialog) {
        ShareDialog(
            noteEntity = NoteEntity(
                title = viewModel.titleState.text.toString(),
                content = viewModel.contentState.text.toString(),
                noteType = editingState.noteType,
                createdAt = editingState.createdAt,
                updatedAt = editingState.updatedAt
            ),
            onDismissRequest = { showShareDialog = false }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            noteEntity = NoteEntity(
                title = viewModel.titleState.text.toString(),
                content = viewModel.contentState.text.toString(),
                noteType = editingState.noteType,
                createdAt = editingState.createdAt,
                updatedAt = editingState.updatedAt
            ),
            onDismissRequest = { showExportDialog = false }
        )
    }
}
