package org.yangdai.kori.presentation.screen.file

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kfile.AudioPicker
import kfile.ImagesPicker
import kfile.PlatformFile
import kfile.VideoPicker
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.completed_tasks
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
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
fun FileScreen(
    viewModel: FileViewModel = koinViewModel(),
    file: PlatformFile,
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {
    val fileEditingState by viewModel.fileEditingState.collectAsStateWithLifecycle()
    val textState by viewModel.textState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val outline by viewModel.outline.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()
    val needSave by viewModel.needSave.collectAsStateWithLifecycle()
    val isAIEnabled by viewModel.isAIEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadFile(file)
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
    var showImagesPicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var showAudioPicker by remember { mutableStateOf(false) }
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

                    Key.S -> {
                        if (needSave) {
                            viewModel.saveFile(file)
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
                    TitleTextField(
                        state = viewModel.titleState,
                        readOnly = true
                    )
                },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(navigateUp) },
                actions = {
                    TooltipIconButton(
                        enabled = needSave,
                        tipText = "Ctrl + S",
                        icon = Icons.Outlined.Save,
                        onClick = { viewModel.saveFile(file) }
                    )

                    if (!isReadView)
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
            AnimatedVisibility(isSearching) {
                FindAndReplaceField(findAndReplaceState)
            }

            if (fileEditingState.fileType == NoteType.Drawing) {
                // 不存在绘画类型文件
            } else {
                AdaptiveEditorViewer(
                    isLargeScreen = isScreenWidthExpanded(),
                    pagerState = pagerState,
                    editor = { modifier ->
                        AdaptiveEditor(
                            modifier = modifier,
                            noteType = fileEditingState.fileType,
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
                    viewer = if (fileEditingState.fileType == NoteType.MARKDOWN || fileEditingState.fileType == NoteType.TODO) { modifier ->
                        AdaptiveViewer(
                            modifier = modifier,
                            noteType = fileEditingState.fileType,
                            html = html,
                            rawText = viewModel.contentState.text.toString(),
                            scrollState = scrollState,
                            isSheetVisible = isSideSheetOpen || showTemplatesBottomSheet,
                            printTrigger = printTrigger
                        )
                    } else null
                )
            }
            AdaptiveEditorRow(
                visible = !isReadView && !isSearching,
                type = fileEditingState.fileType,
                scrollState = scrollState,
                paddingValues = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding(),
                    start = if (isAIEnabled && fileEditingState.fileType != NoteType.PLAIN_TEXT) 52.dp else 0.dp,
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

    TemplatesBottomSheet(
        showTemplatesBottomSheet = showTemplatesBottomSheet,
        onDismissRequest = { showTemplatesBottomSheet = false },
        viewModel = viewModel
    )

    if (showImagesPicker) {
        ImagesPicker("") {
            if (it.isNotEmpty()) viewModel.contentState.edit { addImageLinks(it) }
            showImagesPicker = false
        }
    }

    if (showVideoPicker) {
        VideoPicker("") {
            if (it != null) viewModel.contentState.edit { addVideoLink(it) }
            showVideoPicker = false
        }
    }

    if (showAudioPicker) {
        AudioPicker("") {
            if (it != null) viewModel.contentState.edit { addAudioLink(it) }
            showAudioPicker = false
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = outline,
        type = fileEditingState.fileType,
        onHeaderClick = { selectedHeader = it },
        navigateTo = { navigateToScreen(it) },
        actionContent = {
            IconButton(onClick = { showNoteTypeDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.SwapHorizontalCircle,
                    contentDescription = null
                )
            }

            IconButton(onClick = { viewModel.deleteFile(file) }) {
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

            AnimatedVisibility(fileEditingState.fileType == NoteType.MARKDOWN) {
                IconButton(onClick = { printTrigger.value = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Print,
                        contentDescription = null
                    )
                }
            }
        },
        drawerContent = {
            val formattedUpdated = remember(fileEditingState.updatedAt) {
                if (fileEditingState.updatedAt.isBlank()) ""
                else formatInstant(Instant.parse(fileEditingState.updatedAt))
            }
            NoteSideSheetItem(
                key = stringResource(Res.string.updated),
                value = formattedUpdated
            )

            if (fileEditingState.fileType == NoteType.PLAIN_TEXT || fileEditingState.fileType == NoteType.MARKDOWN) {
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
            } else if (fileEditingState.fileType == NoteType.TODO) {
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
            oNoteType = fileEditingState.fileType,
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
                noteType = fileEditingState.fileType,
                createdAt = fileEditingState.updatedAt,
                updatedAt = fileEditingState.updatedAt
            ),
            onDismissRequest = { showShareDialog = false }
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
