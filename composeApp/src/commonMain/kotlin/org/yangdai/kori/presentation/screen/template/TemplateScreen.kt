package org.yangdai.kori.presentation.screen.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.created
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.line_count
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.paragraph_count
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.todo_text
import kori.composeapp.generated.resources.type
import kori.composeapp.generated.resources.updated
import kori.composeapp.generated.resources.word_count
import kori.composeapp.generated.resources.word_count_without_punctuation
import kotlinx.coroutines.launch
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
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.dialog.ShareDialog
import org.yangdai.kori.presentation.component.note.AdaptiveEditor
import org.yangdai.kori.presentation.component.note.AdaptiveEditorRow
import org.yangdai.kori.presentation.component.note.AdaptiveView
import org.yangdai.kori.presentation.component.note.EditorProperties
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.TitleTextField
import org.yangdai.kori.presentation.component.note.drawing.DrawState
import org.yangdai.kori.presentation.component.note.drawing.InkScreen
import org.yangdai.kori.presentation.component.note.drawing.rememberDrawState
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditor
import org.yangdai.kori.presentation.component.note.rememberFindAndReplaceState
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.util.formatInstant
import org.yangdai.kori.presentation.util.formatNumber
import org.yangdai.kori.presentation.util.isScreenWidthExpanded
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel = koinViewModel(),
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {
    val noteEditingState by viewModel.noteEditingState.collectAsStateWithLifecycle()
    val textState by viewModel.textState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()
    val outline by viewModel.outline.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveOrUpdateNote()
        }
    }

    LaunchedEffect(true) {
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

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isSearching by remember { mutableStateOf(false) }
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    val findAndReplaceState = rememberFindAndReplaceState()
    val pagerState = rememberPagerState { 2 }
    val focusManager = LocalFocusManager.current
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showNoteTypeDialog by rememberSaveable { mutableStateOf(false) }
    var showShareDialog by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    val printTrigger = remember { mutableStateOf(false) }
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
                    TitleTextField(
                        state = viewModel.titleState,
                        readOnly = isReadView
                    )
                },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(navigateUp) },
                actions = {
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

            if (noteEditingState.noteType == NoteType.PLAIN_TEXT) {
                PlainTextEditor(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    textState = viewModel.contentState,
                    scrollState = scrollState,
                    editorProperties = EditorProperties(
                        isReadOnly = isReadView,
                        isLineNumberVisible = editorState.showLineNumber
                    ),
                    findAndReplaceState = findAndReplaceState
                )
            } else if (noteEditingState.noteType == NoteType.Drawing) {
                Text(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .verticalScroll(rememberScrollState())
                        .clickable { isReadView = !isReadView },
                    text = viewModel.contentState.text.toString()
                )
            } else {
                if (isScreenWidthExpanded()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val interactionSource = remember { MutableInteractionSource() }
                        var editorWeight by remember { mutableFloatStateOf(0.5f) }
                        val windowWidth = LocalWindowInfo.current.containerSize.width

                        AdaptiveEditor(
                            modifier = Modifier.fillMaxHeight().weight(editorWeight),
                            type = noteEditingState.noteType,
                            textState = viewModel.contentState,
                            scrollState = scrollState,
                            isReadOnly = isReadView,
                            isLineNumberVisible = editorState.showLineNumber,
                            isLintActive = editorState.isMarkdownLintEnabled,
                            headerRange = selectedHeader,
                            findAndReplaceState = findAndReplaceState
                        )

                        VerticalDragHandle(
                            modifier = Modifier
                                .sizeIn(maxWidth = 12.dp, minWidth = 4.dp)
                                .draggable(
                                    interactionSource = interactionSource,
                                    state = rememberDraggableState { delta ->
                                        editorWeight =
                                            (editorWeight + delta / windowWidth)
                                                .coerceIn(0.15f, 0.85f)
                                    },
                                    orientation = Orientation.Horizontal,
                                    onDragStopped = {
                                        val positions = listOf(0.2f, 1f / 3f, 0.5f, 2f / 3f, 0.8f)
                                        val closest =
                                            positions.minByOrNull { abs(it - editorWeight) }
                                        if (closest != null) {
                                            editorWeight = closest
                                        }
                                    }
                                ),
                            interactionSource = interactionSource
                        )

                        AdaptiveView(
                            modifier = Modifier.fillMaxHeight().weight(1f - editorWeight),
                            noteType = noteEditingState.noteType,
                            contentString = if (noteEditingState.noteType == NoteType.MARKDOWN) html else viewModel.contentState.text.toString(),
                            scrollState = scrollState,
                            isSheetVisible = isSideSheetOpen,
                            printTrigger = printTrigger
                        )
                    }
                } else {
                    HorizontalPager(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        userScrollEnabled = false
                    ) { currentPage ->
                        when (currentPage) {
                            0 -> {
                                AdaptiveEditor(
                                    modifier = Modifier.fillMaxSize(),
                                    type = noteEditingState.noteType,
                                    textState = viewModel.contentState,
                                    scrollState = scrollState,
                                    isReadOnly = isReadView,
                                    isLineNumberVisible = editorState.showLineNumber,
                                    isLintActive = editorState.isMarkdownLintEnabled,
                                    headerRange = selectedHeader,
                                    findAndReplaceState = findAndReplaceState
                                )
                            }

                            1 -> {
                                AdaptiveView(
                                    modifier = Modifier.fillMaxSize(),
                                    noteType = noteEditingState.noteType,
                                    contentString = if (noteEditingState.noteType == NoteType.MARKDOWN) html else viewModel.contentState.text.toString(),
                                    scrollState = scrollState,
                                    isSheetVisible = isSideSheetOpen,
                                    printTrigger = printTrigger
                                )
                            }
                        }
                    }
                }
            }
            AdaptiveEditorRow(
                visible = !isReadView && !isSearching,
                type = noteEditingState.noteType,
                scrollState = scrollState,
                bottomPadding = innerPadding.calculateBottomPadding(),
                textFieldState = viewModel.contentState,
                isTemplate = true
            )
        }
    }

    AnimatedVisibility(
        visible = noteEditingState.noteType == NoteType.Drawing && !isReadView,
        enter = scaleIn(initialScale = 0.9f),
        exit = scaleOut(targetScale = 0.9f)
    ) {
        val drawState = rememberDrawState(viewModel.contentState.text.toString())
        InkScreen(drawState, noteEditingState.id) {
            viewModel.contentState.setTextAndPlaceCursorAtEnd(DrawState.serializeDrawState(drawState))
            isReadView = true
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = outline,
        showOutline = noteEditingState.noteType == NoteType.MARKDOWN,
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

            AnimatedVisibility(noteEditingState.noteType == NoteType.MARKDOWN) {
                Column {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { printTrigger.value = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Print,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        drawerContent = {
            NoteSideSheetItem(
                key = stringResource(Res.string.type),
                value = when (noteEditingState.noteType) {
                    NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                    NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                    NoteType.TODO -> stringResource(Res.string.todo_text)
                    NoteType.Drawing -> stringResource(Res.string.drawing)
                }
            )

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
}
