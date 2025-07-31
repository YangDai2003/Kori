package org.yangdai.kori.presentation.screen.file

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHorizontalCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kfile.PlatformFile
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.line_count
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.paragraph_count
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.saveAsTemplate
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.title
import kori.composeapp.generated.resources.todo_text
import kori.composeapp.generated.resources.type
import kori.composeapp.generated.resources.updated
import kori.composeapp.generated.resources.word_count
import kori.composeapp.generated.resources.word_count_without_punctuation
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.dialog.ShareDialog
import org.yangdai.kori.presentation.component.note.AdaptiveEditor
import org.yangdai.kori.presentation.component.note.AdaptiveEditorRow
import org.yangdai.kori.presentation.component.note.AdaptiveView
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.moveCursorLeftStateless
import org.yangdai.kori.presentation.component.note.moveCursorRightStateless
import org.yangdai.kori.presentation.component.note.rememberFindAndReplaceState
import org.yangdai.kori.presentation.component.note.template.TemplateProcessor
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.util.formatInstant
import org.yangdai.kori.presentation.util.formatNumber
import org.yangdai.kori.presentation.util.isScreenWidthExpanded
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalTime::class
)
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
    val formatterState by viewModel.formatterState.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val outline by viewModel.outline.collectAsStateWithLifecycle()
    val html by viewModel.html.collectAsStateWithLifecycle()
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isAppInDarkTheme = remember(appTheme, isSystemInDarkTheme) {
        if (appTheme == AppTheme.SYSTEM) isSystemInDarkTheme
        else appTheme == AppTheme.DARK
    }
    val needSave by viewModel.needSave.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadFile(file)
    }

    LaunchedEffect(true) {
        viewModel.uiEventFlow.collect { event ->
            if (event is UiEvent.NavigateUp) navigateUp()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isReadView by rememberSaveable { mutableStateOf(false) }
    var showTemplatesBottomSheet by rememberSaveable { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedHeader by remember { mutableStateOf<IntRange?>(null) }
    val findAndReplaceState = rememberFindAndReplaceState()
    val pagerState = rememberPagerState { 2 }
    val focusManager = LocalFocusManager.current
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showNoteTypeDialog by rememberSaveable { mutableStateOf(false) }
    var showShareDialog by rememberSaveable { mutableStateOf(false) }
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
                    BasicTextField(
                        modifier = Modifier.onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.DirectionLeft -> {
                                        if (currentPlatformInfo.operatingSystem == OS.ANDROID) {
                                            viewModel.titleState.edit { moveCursorLeftStateless() }
                                            true
                                        } else false
                                    }

                                    Key.DirectionRight -> {
                                        if (currentPlatformInfo.operatingSystem == OS.ANDROID) {
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
                        readOnly = true,
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

            if (fileEditingState.fileType == NoteType.PLAIN_TEXT) {
                AdaptiveEditor(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    type = fileEditingState.fileType,
                    textState = viewModel.contentState,
                    scrollState = scrollState,
                    isReadOnly = isReadView,
                    isLineNumberVisible = editorState.showLineNumber,
                    isLintActive = editorState.isMarkdownLintEnabled,
                    headerRange = selectedHeader,
                    findAndReplaceState = findAndReplaceState
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
                            type = fileEditingState.fileType,
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
                            noteType = fileEditingState.fileType,
                            contentString = if (fileEditingState.fileType == NoteType.MARKDOWN) html else viewModel.contentState.text.toString(),
                            scrollState = scrollState,
                            isAppInDarkTheme = isAppInDarkTheme,
                            isSheetVisible = isSideSheetOpen || showTemplatesBottomSheet,
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
                                    type = fileEditingState.fileType,
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
                                    noteType = fileEditingState.fileType,
                                    contentString = if (fileEditingState.fileType == NoteType.MARKDOWN) html else viewModel.contentState.text.toString(),
                                    scrollState = scrollState,
                                    isAppInDarkTheme = isAppInDarkTheme,
                                    isSheetVisible = isSideSheetOpen || showTemplatesBottomSheet,
                                    printTrigger = printTrigger
                                )
                            }
                        }
                    }
                }
            }
            AdaptiveEditorRow(
                visible = !isReadView,
                type = fileEditingState.fileType,
                scrollState = scrollState,
                bottomPadding = innerPadding.calculateBottomPadding(),
                textFieldState = viewModel.contentState
            ) { action ->
                when (action) {
                    EditorRowAction.Templates -> {
                        showTemplatesBottomSheet = true
                    }
                }
            }
        }
    }

    val templatesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideTemplatesBottomSheet: () -> Unit = {
        coroutineScope.launch {
            templatesSheetState.hide()
        }.invokeOnCompletion {
            if (!templatesSheetState.isVisible) {
                showTemplatesBottomSheet = false
            }
        }
    }
    if (showTemplatesBottomSheet) {
        ModalBottomSheet(
            sheetState = templatesSheetState,
            sheetGesturesEnabled = false,
            onDismissRequest = { showTemplatesBottomSheet = false },
            dragHandle = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.templates),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IconButton(
                        modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                            .minimumInteractiveComponentSize()
                            .size(
                                IconButtonDefaults.extraSmallContainerSize(
                                    IconButtonDefaults.IconButtonWidthOption.Uniform
                                )
                            ),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        onClick = hideTemplatesBottomSheet
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                        )
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(templates, key = { it.id }) { template ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = template.title,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(CircleShape)
                            .clickable {
                                val templateText = TemplateProcessor(
                                    formatterState.dateFormatter, formatterState.timeFormatter,
                                ).process(template.content)
                                viewModel.contentState.edit { appendLine(templateText) }
                                hideTemplatesBottomSheet()
                            }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.saveNoteAsTemplate() }) {
                        Text(stringResource(Res.string.saveAsTemplate))
                    }
                }
            }
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = outline,
        showOutline = fileEditingState.fileType == NoteType.MARKDOWN,
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
                        imageVector = if (currentPlatformInfo.operatingSystem == OS.ANDROID) Icons.Outlined.Share
                        else Icons.Outlined.IosShare,
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
            NoteSideSheetItem(
                key = stringResource(Res.string.type),
                value = when (fileEditingState.fileType) {
                    NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                    NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                    NoteType.TODO -> stringResource(Res.string.todo_text)
                }
            )

            val formattedUpdated = remember(fileEditingState.updatedAt) {
                if (fileEditingState.updatedAt.isBlank()) ""
                else formatInstant(Instant.parse(fileEditingState.updatedAt))
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
}
