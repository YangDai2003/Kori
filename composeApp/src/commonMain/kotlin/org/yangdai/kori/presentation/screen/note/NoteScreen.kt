package org.yangdai.kori.presentation.screen.note

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHorizontalCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kfile.AudioPicker
import kfile.ImagesPicker
import kfile.VideoPicker
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.completed_tasks
import kori.composeapp.generated.resources.created
import kori.composeapp.generated.resources.line_count
import kori.composeapp.generated.resources.paragraph_count
import kori.composeapp.generated.resources.pending_tasks
import kori.composeapp.generated.resources.progress
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.total_tasks
import kori.composeapp.generated.resources.updated
import kori.composeapp.generated.resources.word_count
import kori.composeapp.generated.resources.word_count_without_punctuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.ExportDialog
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.dialog.ShareDialog
import org.yangdai.kori.presentation.component.dialog.TemplatesBottomSheet
import org.yangdai.kori.presentation.component.note.AdaptiveEditor
import org.yangdai.kori.presentation.component.note.AdaptiveEditorRow
import org.yangdai.kori.presentation.component.note.AdaptiveEditorViewer
import org.yangdai.kori.presentation.component.note.AdaptiveViewer
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.GenerateNoteButton
import org.yangdai.kori.presentation.component.note.LoadingScrim
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.TitleTextField
import org.yangdai.kori.presentation.component.note.addAudioLink
import org.yangdai.kori.presentation.component.note.addImageLinks
import org.yangdai.kori.presentation.component.note.addVideoLink
import org.yangdai.kori.presentation.component.note.drawing.DrawState
import org.yangdai.kori.presentation.component.note.drawing.InNoteDrawPreview
import org.yangdai.kori.presentation.component.note.drawing.InkScreen
import org.yangdai.kori.presentation.component.note.drawing.rememberDrawState
import org.yangdai.kori.presentation.component.note.rememberFindAndReplaceState
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.util.formatInstant
import org.yangdai.kori.presentation.util.formatNumber
import org.yangdai.kori.presentation.util.isScreenWidthExpanded
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel = koinViewModel(),
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {
    val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsStateWithLifecycle()
    val noteEditingState by viewModel.noteEditingState.collectAsStateWithLifecycle()
    val textState by viewModel.textState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val outline by viewModel.outline.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()
    val isAIEnabled by viewModel.isAIEnabled.collectAsStateWithLifecycle()

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
        if (editorState.isDefaultReadingView && noteEditingState.id.isNotEmpty()) {
            isReadView = true
        }
    }

    var folderName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(noteEditingState.folderId, foldersWithNoteCounts) {
        withContext(Dispatchers.Default) {
            folderName = if (noteEditingState.folderId == null) {
                getString(Res.string.all_notes)
            } else {
                foldersWithNoteCounts.find { it.folder.id == noteEditingState.folderId }?.folder?.name
                    ?: getString(Res.string.all_notes)
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState { 2 }
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val findAndReplaceState = rememberFindAndReplaceState()
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    var showNoteTypeDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showTemplatesBottomSheet by remember { mutableStateOf(false) }
    var showImagesPicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var showAudioPicker by remember { mutableStateOf(false) }
    val printTrigger = remember { mutableStateOf(false) }
    val cachedImageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val isLargeScreen = isScreenWidthExpanded()

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

                    Key.P -> {
                        isReadView = !isReadView
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
            TopAppBar(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    )
                },
                expandedHeight = 56.dp,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            modifier = Modifier.sizeIn(maxWidth = 160.dp),
                            onClick = { showFolderDialog = true }
                        ) {
                            Text(
                                text = folderName, maxLines = 1, modifier = Modifier.basicMarquee()
                            )
                        }
                        if (isLargeScreen)
                            TitleTextField(
                                state = viewModel.titleState,
                                readOnly = isReadView && noteEditingState.noteType != NoteType.Drawing,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                    }
                },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(navigateUp) },
                actions = {
                    if (!isReadView && noteEditingState.noteType != NoteType.Drawing)
                        TooltipIconButton(
                            tipText = "Ctrl + F",
                            icon = if (isSearching) Icons.Default.SearchOff
                            else Icons.Default.Search,
                            onClick = { isSearching = !isSearching }
                        )

                    TooltipIconButton(
                        tipText = "Ctrl + P",
                        icon = if (isReadView) Icons.Default.EditNote
                        else Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { isReadView = !isReadView }
                    )

                    TooltipIconButton(
                        tipText = "Ctrl + Tab",
                        icon = painterResource(Res.drawable.right_panel_open),
                        onClick = { isSideSheetOpen = true }
                    )
                }
            )
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection)
            )
        ) {
            if (isLargeScreen)
                AnimatedVisibility(isSearching) {
                    FindAndReplaceField(findAndReplaceState)
                }
            else
                AnimatedContent(isSearching) { targetState ->
                    if (targetState)
                        FindAndReplaceField(findAndReplaceState)
                    else {
                        TitleTextField(
                            state = viewModel.titleState,
                            readOnly = isReadView && noteEditingState.noteType != NoteType.Drawing,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    }
                }

            if (noteEditingState.noteType == NoteType.Drawing) {
                InNoteDrawPreview(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                        .clickable { isReadView = !isReadView },
                    imageBitmap = cachedImageBitmap.value,
                    uuid = noteEditingState.id
                )
            } else {
                AdaptiveEditorViewer(
                    isLargeScreen = isLargeScreen,
                    pagerState = pagerState,
                    editor = { modifier ->
                        AdaptiveEditor(
                            modifier = modifier,
                            noteType = noteEditingState.noteType,
                            textFieldState = viewModel.contentState,
                            scrollState = scrollState,
                            isReadOnly = isReadView,
                            isLineNumberVisible = editorState.showLineNumber,
                            isLintActive = editorState.isMarkdownLintEnabled,
                            headerRange = selectedHeader,
                            findAndReplaceState = findAndReplaceState,
                            isAIEnabled = isAIEnabled,
                            onAIContextMenuEvent = { viewModel.onAIContextMenuEvent(it) }
                        )
                    },
                    viewer = if (noteEditingState.noteType == NoteType.MARKDOWN || noteEditingState.noteType == NoteType.TODO) { modifier ->
                        AdaptiveViewer(
                            modifier = modifier,
                            noteType = noteEditingState.noteType,
                            html = html,
                            rawText = viewModel.contentState.text.toString(),
                            scrollState = scrollState,
                            isSheetVisible = isSideSheetOpen || showFolderDialog || showTemplatesBottomSheet,
                            printTrigger = printTrigger
                        )
                    } else null
                )
            }
            AdaptiveEditorRow(
                visible = !isReadView && !isSearching,
                type = noteEditingState.noteType,
                scrollState = scrollState,
                paddingValues = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding(),
                    start = if (isAIEnabled && noteEditingState.noteType != NoteType.PLAIN_TEXT) 52.dp else 0.dp,
                ),
                textFieldState = viewModel.contentState
            ) { action ->
                when (action) {
                    EditorRowAction.Templates -> showTemplatesBottomSheet = true
                    EditorRowAction.Images -> showImagesPicker = true
                    EditorRowAction.Videos -> showVideoPicker = true
                    EditorRowAction.Audio -> showAudioPicker = true
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isAIEnabled && !isReadView && !isSearching,
        enter = fadeIn() + slideInHorizontally { -it },
        exit = fadeOut() + slideOutHorizontally { -it }
    ) {
        GenerateNoteButton(
            startGenerating = { prompt, onSuccess, onError ->
                if (prompt.isNotBlank())
                    viewModel.generateNoteFromPrompt(prompt, onSuccess, onError)
                else
                    onError("Prompt cannot be empty")
            }
        )
    }

    AnimatedVisibility(
        visible = noteEditingState.noteType == NoteType.Drawing && !isReadView,
        enter = scaleIn(initialScale = 0.95f) + slideInVertically { it / 20 } + fadeIn(),
        exit = scaleOut(targetScale = 0.95f) + slideOutVertically { it / 20 } + fadeOut()
    ) {
        val drawState = rememberDrawState(viewModel.contentState.text.toString())
        InkScreen(drawState, noteEditingState.id, cachedImageBitmap) {
            viewModel.contentState.setTextAndPlaceCursorAtEnd(DrawState.serializeDrawState(drawState))
            isReadView = true
        }
    }

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

    TemplatesBottomSheet(
        showTemplatesBottomSheet = showTemplatesBottomSheet,
        onDismissRequest = { showTemplatesBottomSheet = false },
        viewModel = viewModel
    )

    if (showImagesPicker) {
        ImagesPicker(noteEditingState.id) {
            if (it.isNotEmpty()) viewModel.contentState.edit { addImageLinks(it) }
            showImagesPicker = false
        }
    }

    if (showVideoPicker) {
        VideoPicker(noteEditingState.id) {
            if (it != null) viewModel.contentState.edit { addVideoLink(it) }
            showVideoPicker = false
        }
    }

    if (showAudioPicker) {
        AudioPicker(noteEditingState.id) {
            if (it != null) viewModel.contentState.edit { addAudioLink(it) }
            showAudioPicker = false
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = outline,
        type = noteEditingState.noteType,
        onHeaderClick = { selectedHeader = it },
        navigateTo = { navigateToScreen(it) },
        actionContent = {
            val hapticFeedback = LocalHapticFeedback.current
            IconToggleButton(
                checked = noteEditingState.isPinned,
                onCheckedChange = {
                    if (it) hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    else hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                    viewModel.toggleNotePin()
                }
            ) {
                Icon(
                    imageVector = if (noteEditingState.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = if (noteEditingState.isPinned) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current
                )
            }

            IconButton(onClick = { viewModel.moveNoteToTrash() }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
                )
            }

            if (noteEditingState.noteType != NoteType.Drawing) {
                IconButton(onClick = { showNoteTypeDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHorizontalCircle,
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
            }

            AnimatedVisibility(noteEditingState.noteType == NoteType.MARKDOWN) {
                IconButton(onClick = { printTrigger.value = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Print,
                        contentDescription = null
                    )
                }
            }
        },
        drawerContent = {
            val formattedCreated = remember(noteEditingState.createdAt) {
                if (noteEditingState.createdAt.isBlank()) ""
                else formatInstant(Instant.parse(noteEditingState.createdAt))
            }
            NoteSideSheetItem(
                key = stringResource(Res.string.created),
                value = formattedCreated
            )
            val formattedUpdated = remember(noteEditingState.updatedAt) {
                if (noteEditingState.updatedAt.isBlank()) ""
                else formatInstant(Instant.parse(noteEditingState.updatedAt))
            }
            NoteSideSheetItem(
                key = stringResource(Res.string.updated),
                value = formattedUpdated
            )

            if (noteEditingState.noteType == NoteType.PLAIN_TEXT || noteEditingState.noteType == NoteType.MARKDOWN) {
                /**文本文件信息：字符数，单词数，行数，段落数**/
                NoteSideSheetItem(
                    key = stringResource(Res.string.char_count),
                    value = formatNumber(textState.charCount)
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.word_count),
                    value = formatNumber(textState.wordCountWithPunctuation)
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.word_count_without_punctuation),
                    value = formatNumber(textState.wordCountWithoutPunctuation)
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.line_count),
                    value = formatNumber(textState.lineCount)
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.paragraph_count),
                    value = formatNumber(textState.paragraphCount)
                )
            } else if (noteEditingState.noteType == NoteType.TODO) {
                /**总任务，已完成，待办，进度**/
                var totalTasks by remember { mutableIntStateOf(0) }
                var completedTasks by remember { mutableIntStateOf(0) }
                var pendingTasks by remember { mutableIntStateOf(0) }
                var progress by remember { mutableIntStateOf(0) }
                LaunchedEffect(viewModel.contentState.text) {
                    withContext(Dispatchers.Default) {
                        val lines = viewModel.contentState.text.lines()
                        totalTasks = lines.count { it.isNotBlank() }
                        completedTasks =
                            lines.count { it.trim().startsWith("x", ignoreCase = true) }
                        pendingTasks = totalTasks - completedTasks
                        progress = if (totalTasks > 0) {
                            (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt()
                        } else {
                            0
                        }
                    }
                }
                NoteSideSheetItem(
                    key = stringResource(Res.string.total_tasks),
                    value = totalTasks.toString()
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.completed_tasks),
                    value = completedTasks.toString()
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.pending_tasks),
                    value = pendingTasks.toString()
                )
                NoteSideSheetItem(
                    key = stringResource(Res.string.progress),
                    value = "$progress%"
                )
            }
        }
    )

    if (showNoteTypeDialog) {
        NoteTypeDialog(
            oNoteType = noteEditingState.noteType,
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
                noteType = noteEditingState.noteType,
                createdAt = noteEditingState.createdAt,
                updatedAt = noteEditingState.updatedAt
            ),
            onDismissRequest = { showShareDialog = false }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            noteEntity = NoteEntity(
                title = viewModel.titleState.text.toString(),
                content = viewModel.contentState.text.toString(),
                noteType = noteEditingState.noteType,
                createdAt = noteEditingState.createdAt,
                updatedAt = noteEditingState.updatedAt
            ),
            html = html,
            onDismissRequest = { showExportDialog = false }
        )
    }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    AnimatedVisibility(
        visible = isGenerating,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LoadingScrim()
    }
}
