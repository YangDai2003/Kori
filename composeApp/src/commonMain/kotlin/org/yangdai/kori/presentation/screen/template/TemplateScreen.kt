package org.yangdai.kori.presentation.screen.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHorizontalCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.created
import kori.composeapp.generated.resources.line_count
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.paragraph_count
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.title
import kori.composeapp.generated.resources.type
import kori.composeapp.generated.resources.updated
import kori.composeapp.generated.resources.word_count
import kori.composeapp.generated.resources.word_count_without_punctuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.OS
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.ExportDialog
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.dialog.ShareDialog
import org.yangdai.kori.presentation.component.note.Editor
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.markdown.MarkdownView
import org.yangdai.kori.presentation.component.note.markdown.moveCursorLeftStateless
import org.yangdai.kori.presentation.component.note.markdown.moveCursorRightStateless
import org.yangdai.kori.presentation.component.note.template.TemplateEditorRow
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.util.formatInstant
import org.yangdai.kori.presentation.util.formatNumber
import org.yangdai.kori.presentation.util.isScreenSizeLarge
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
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
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isAppInDarkTheme = remember(appTheme, isSystemInDarkTheme) {
        if (appTheme == AppTheme.SYSTEM) isSystemInDarkTheme
        else appTheme == AppTheme.DARK
    }

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

    var isSearching by remember { mutableStateOf(false) }
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    var findAndReplaceState by remember { mutableStateOf(FindAndReplaceState()) }
    LaunchedEffect(findAndReplaceState.searchWord, viewModel.contentState.text) {
        withContext(Dispatchers.Default) {
            findAndReplaceState = findAndReplaceState.copy(
                matchCount = if (findAndReplaceState.searchWord.isNotBlank())
                    findAndReplaceState.searchWord.toRegex()
                        .findAll(viewModel.contentState.text)
                        .count()
                else 0
            )
        }
    }


    val isLargeScreen = isScreenSizeLarge()
    val pagerState = rememberPagerState { 2 }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showNoteTypeDialog by rememberSaveable { mutableStateOf(false) }
    var showShareDialog by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    val printTrigger = remember { mutableStateOf(false) }
    LaunchedEffect(isReadView) {
        keyboardController?.hide()
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
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        true
                    }

                    else -> false
                }
            } else false
        },
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionLeft -> {
                                            if (currentPlatformInfo.platform == Platform.Android) {
                                                viewModel.titleState.edit { moveCursorLeftStateless() }
                                                true
                                            } else false
                                        }

                                        Key.DirectionRight -> {
                                            if (currentPlatformInfo.platform == Platform.Android) {
                                                viewModel.titleState.edit { moveCursorRightStateless() }
                                                true
                                            } else false
                                        }

                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            },
                        state = viewModel.titleState,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        readOnly = isReadView,
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
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    )
                                },
                                contentPadding = PaddingValues(0.dp),
                                container = {}
                            )
                        }
                    )
                },
                navigationIcon = {
                    PlatformStyleTopAppBarNavigationIcon(onClick = navigateUp)
                },
                actions = {
                    if (!isReadView)
                        TooltipIconButton(
                            tipText = "Ctrl + F",
                            icon = Icons.Outlined.Search,
                            onClick = { isSearching = !isSearching }
                        )

                    TooltipIconButton(
                        tipText = "Ctrl + P",
                        icon = if (isReadView) Icons.Outlined.EditNote
                        else Icons.AutoMirrored.Outlined.MenuBook,
                        onClick = { isReadView = !isReadView }
                    )

                    TooltipIconButton(
                        tipText = "Ctrl + Tab",
                        icon = painterResource(Res.drawable.right_panel_open),
                        onClick = {
                            isSideSheetOpen = true
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            AnimatedVisibility(isSearching) {
                FindAndReplaceField(
                    state = findAndReplaceState,
                    onStateUpdate = { findAndReplaceState = it }
                )
            }

            val scrollState = rememberScrollState()
            if (noteEditingState.noteType == NoteType.PLAIN_TEXT) {
                Editor(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    isTemplate = true,
                    type = noteEditingState.noteType,
                    state = viewModel.contentState,
                    scrollState = scrollState,
                    readMode = isReadView,
                    showLineNumbers = editorState.showLineNumber,
                    isLintActive = editorState.isMarkdownLintEnabled,
                    headerRange = selectedHeader,
                    findAndReplaceState = findAndReplaceState,
                    onFindAndReplaceUpdate = { findAndReplaceState = it }
                )
            } else {
                if (isLargeScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val interactionSource = remember { MutableInteractionSource() }
                        var editorWeight by remember { mutableFloatStateOf(0.5f) }
                        val windowWidth = LocalWindowInfo.current.containerSize.width

                        Editor(
                            modifier = Modifier.fillMaxHeight().weight(editorWeight),
                            isTemplate = true,
                            type = noteEditingState.noteType,
                            state = viewModel.contentState,
                            scrollState = scrollState,
                            readMode = isReadView,
                            showLineNumbers = editorState.showLineNumber,
                            isLintActive = editorState.isMarkdownLintEnabled,
                            headerRange = selectedHeader,
                            findAndReplaceState = findAndReplaceState,
                            onFindAndReplaceUpdate = { findAndReplaceState = it }
                        )

                        VerticalDragHandle(
                            modifier = Modifier
                                .sizeIn(maxWidth = 12.dp, minWidth = 4.dp)
                                .draggable(
                                    interactionSource = interactionSource,
                                    state = rememberDraggableState { delta ->
                                        editorWeight =
                                            (editorWeight + delta / windowWidth).coerceIn(
                                                0.3f, 0.7f
                                            )
                                    },
                                    orientation = Orientation.Horizontal,
                                    onDragStopped = {
                                        val positions = listOf(1f / 3f, 0.5f, 2f / 3f)
                                        val closest =
                                            positions.minByOrNull { abs(it - editorWeight) }
                                        if (closest != null) {
                                            editorWeight = closest
                                        }
                                    }
                                ),
                            interactionSource = interactionSource
                        )

                        MarkdownView(
                            modifier = Modifier.fillMaxHeight().weight(1f - editorWeight),
                            html = html,
                            selection = viewModel.contentState.selection,
                            scrollState = scrollState,
                            isAppInDarkTheme = isAppInDarkTheme,
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
                                Editor(
                                    modifier = Modifier.fillMaxSize(),
                                    isTemplate = true,
                                    type = noteEditingState.noteType,
                                    state = viewModel.contentState,
                                    scrollState = scrollState,
                                    readMode = isReadView,
                                    showLineNumbers = editorState.showLineNumber,
                                    isLintActive = editorState.isMarkdownLintEnabled,
                                    headerRange = selectedHeader,
                                    findAndReplaceState = findAndReplaceState,
                                    onFindAndReplaceUpdate = { findAndReplaceState = it }
                                )
                            }

                            1 -> {
                                MarkdownView(
                                    modifier = Modifier.fillMaxSize(),
                                    html = html,
                                    selection = viewModel.contentState.selection,
                                    scrollState = scrollState,
                                    isAppInDarkTheme = isAppInDarkTheme,
                                    isSheetVisible = isSideSheetOpen,
                                    printTrigger = printTrigger
                                )
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = !isReadView) {
                TemplateEditorRow(viewModel.contentState)
            }
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = HeaderNode("", 0, IntRange.EMPTY),
        showOutline = false,
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

            if (currentPlatformInfo.platform != Platform.Desktop)
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(
                        imageVector = if (currentPlatformInfo.platform == Platform.Android) Icons.Outlined.Share
                        else Icons.Outlined.IosShare,
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
                    if (
                        currentPlatformInfo.operatingSystem == OS.IOS || currentPlatformInfo.operatingSystem == OS.ANDROID
                        || currentPlatformInfo.operatingSystem == OS.WINDOWS
                    )
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
