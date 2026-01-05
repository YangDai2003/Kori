package org.yangdai.kori.presentation.screen.file

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHorizontalCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.find
import kori.composeapp.generated.resources.replace
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.save
import kori.composeapp.generated.resources.side_sheet
import kori.composeapp.generated.resources.updated
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.SingleRowTopAppBar
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.dialog.ShareDialog
import org.yangdai.kori.presentation.component.dialog.TemplatesBottomSheet
import org.yangdai.kori.presentation.component.note.AIAssist
import org.yangdai.kori.presentation.component.note.AdaptiveActionRow
import org.yangdai.kori.presentation.component.note.AdaptiveEditor
import org.yangdai.kori.presentation.component.note.AdaptiveEditorViewer
import org.yangdai.kori.presentation.component.note.AdaptiveViewer
import org.yangdai.kori.presentation.component.note.EditorScaffold
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.TitleText
import org.yangdai.kori.presentation.component.note.rememberFindAndReplaceState
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.util.formatInstant
import org.yangdai.kori.presentation.util.rememberIsScreenWidthExpanded
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FileScreen(
    viewModel: FileViewModel,
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {
    val editingState by viewModel.editingState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val needSave by viewModel.needSave.collectAsStateWithLifecycle()
    val showAI by viewModel.showAI.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEventFlow.collect { event ->
            if (event is UiEvent.NavigateUp) navigateUp()
        }
    }

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isReadView by rememberSaveable { mutableStateOf(false) }
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showTemplatesBottomSheet by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val findAndReplaceState = rememberFindAndReplaceState()
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    var showNoteTypeDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val printTrigger = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(isReadView) {
        focusManager.clearFocus()
        isSearching = false
        pagerState.animateScrollToPage(if (isReadView) 1 else 0)
    }

    EditorScaffold(
        modifier = Modifier.onPreviewKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                when (keyEvent.key) {
                    Key.F -> {
                        isSearching = !isSearching
                        true
                    }

                    Key.S -> {
                        if (needSave) {
                            viewModel.saveFile()
                        }
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
                title = { TitleText(viewModel.titleState) },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(navigateUp) },
                actions = {
                    TooltipIconButton(
                        enabled = needSave,
                        hint = stringResource(Res.string.save),
                        actionText = "S",
                        icon = Icons.Outlined.Save,
                        onClick = { viewModel.saveFile() }
                    )

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
                noteType = editingState.fileType,
                noteId = "",
                scrollState = scrollState,
                contentState = viewModel.contentState,
                showAIAssistPlaceholder = showAI,
                onTemplatesAction = { showTemplatesBottomSheet = true }
            )
        }
    ) {
        AnimatedVisibility(isSearching) {
            FindAndReplaceField(findAndReplaceState)
        }

        if (editingState.fileType != NoteType.Drawing) {
            var firstVisibleCharPosition by remember { mutableIntStateOf(0) }
            AdaptiveEditorViewer(
                showDualPane = rememberIsScreenWidthExpanded(),
                pagerState = pagerState,
                defaultEditorWeight = editorState.editorWeight,
                onEditorWeightChanged = { viewModel.changeDefaultEditorWeight(it) },
                editor = { modifier ->
                    AdaptiveEditor(
                        modifier = modifier,
                        noteType = editingState.fileType,
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
                viewer = if (editingState.fileType == NoteType.MARKDOWN || editingState.fileType == NoteType.TODO) { modifier ->
                    AdaptiveViewer(
                        modifier = modifier,
                        noteType = editingState.fileType,
                        textFieldState = viewModel.contentState,
                        firstVisibleCharPosition = firstVisibleCharPosition,
                        isSheetVisible = isSideSheetOpen || showTemplatesBottomSheet,
                        printTrigger = printTrigger
                    )
                } else null
            )
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

    TemplatesBottomSheet(
        showTemplatesBottomSheet = showTemplatesBottomSheet,
        onDismissRequest = { showTemplatesBottomSheet = false },
        viewModel = viewModel
    )

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        text = viewModel.contentState.text.toString(),
        noteType = editingState.fileType,
        onHeaderClick = { selectedHeader = it },
        navigateTo = { navigateToScreen(it) },
        actionContent = {
            IconButton(onClick = { showNoteTypeDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.SwapHorizontalCircle,
                    contentDescription = null
                )
            }

            IconButton(onClick = { viewModel.deleteFile() }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
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

            AnimatedVisibility(editingState.fileType == NoteType.MARKDOWN) {
                IconButton(onClick = { printTrigger.value = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Print,
                        contentDescription = null
                    )
                }
            }
        },
        drawerContent = {
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
            oNoteType = editingState.fileType,
            onDismissRequest = { showNoteTypeDialog = false },
            onNoteTypeSelected = { noteType ->
                showNoteTypeDialog = false
                viewModel.updateFileType(noteType)
            }
        )
    }

    if (showShareDialog) {
        ShareDialog(
            noteEntity = NoteEntity(
                title = viewModel.titleState.text.toString(),
                content = viewModel.contentState.text.toString(),
                noteType = editingState.fileType,
                createdAt = editingState.updatedAt,
                updatedAt = editingState.updatedAt
            ),
            onDismissRequest = { showShareDialog = false }
        )
    }
}
