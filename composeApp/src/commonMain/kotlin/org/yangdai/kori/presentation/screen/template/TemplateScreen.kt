package org.yangdai.kori.presentation.screen.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHorizontalCircle
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.created
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.line_count
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.paragraph_count
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.right_panel_open
import kori.composeapp.generated.resources.type
import kori.composeapp.generated.resources.updated
import kori.composeapp.generated.resources.word_count
import kori.composeapp.generated.resources.word_count_without_punctuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.note.EditorRowAction
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditor
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditorRow
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditor
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditorRow
import org.yangdai.kori.presentation.event.UiEvent
import org.yangdai.kori.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, FormatStringsInDatetimeFormats::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel = koinViewModel(),
    noteId: String,
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {

    val noteEditingState by viewModel.noteEditingState.collectAsStateWithLifecycle()
    val textState by viewModel.textState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val formatterState by viewModel.formatterState.collectAsStateWithLifecycle()

    // 确保屏幕旋转等配置变更时，不会重复加载笔记
    var lastLoadedNoteId by rememberSaveable { mutableStateOf<String?>(null) }

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

    val keyboardController = LocalSoftwareKeyboardController.current
    var isSideSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showNoteTypeDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.titleState.text.toString(),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                },
                navigationIcon = {
                    PlatformStyleTopAppBarNavigationIcon(onClick = navigateUp)
                },
                actions = {

                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = {
                        isSideSheetOpen = true
                        keyboardController?.hide()
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.right_panel_open),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedVisibility(visible = isSearching) {
                FindAndReplaceField(
                    state = findAndReplaceState,
                    onStateUpdate = { findAndReplaceState = it }
                )
            }

            val scrollState = rememberScrollState()
            when (noteEditingState.noteType) {
                NoteType.PLAIN_TEXT -> {
                    Column(Modifier.fillMaxSize().weight(1f)) {
                        PlainTextEditor(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            state = viewModel.contentState,
                            scrollState = scrollState,
                            readMode = true,
                            showLineNumbers = editorState.showLineNumber,
                            findAndReplaceState = findAndReplaceState,
                            onFindAndReplaceUpdate = { findAndReplaceState = it }
                        )
                        PlainTextEditorRow(viewModel.contentState) { action ->
                            when (action) {
                                EditorRowAction.Templates -> {

                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(Modifier.fillMaxSize().weight(1f)) {
                        MarkdownEditor(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            state = viewModel.contentState,
                            scrollState = scrollState,
                            readMode = true,
                            showLineNumbers = editorState.showLineNumber,
                            isLintActive = editorState.isMarkdownLintEnabled,
                            headerRange = selectedHeader,
                            findAndReplaceState = findAndReplaceState,
                            onFindAndReplaceUpdate = { findAndReplaceState = it }
                        )
                        MarkdownEditorRow(viewModel.contentState) { action ->
                            when (action) {
                                EditorRowAction.Templates -> {

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    NoteSideSheet(
        isDrawerOpen = isSideSheetOpen,
        onDismiss = { isSideSheetOpen = false },
        outline = HeaderNode(title = "test", level = 1, range = IntRange(0, 0)),
        onHeaderClick = { selectedHeader = it },
        navigateTo = { navigateToScreen(it) },
        actionContent = {
            IconToggleButton(
                checked = noteEditingState.isPinned,
                onCheckedChange = { viewModel.toggleNotePin() }
            ) {
                Icon(
                    imageVector = if (noteEditingState.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = if (noteEditingState.isPinned) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current
                )
            }

            IconButton(onClick = { showNoteTypeDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.SwapHorizontalCircle,
                    contentDescription = null
                )
            }

            TooltipIconButton(
                tipText = stringResource(Res.string.delete),
                icon = Icons.Outlined.Delete,
                onClick = { viewModel.moveNoteToTrash() }
            )
        },
        drawerContent = {
            NoteSideSheetItem(
                key = stringResource(Res.string.type),
                value = when (noteEditingState.noteType) {
                    NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                    NoteType.LITE_MARKDOWN -> stringResource(Res.string.markdown) + " (Lite)"
                    NoteType.STANDARD_MARKDOWN -> stringResource(Res.string.markdown) + " (Standard)"
                }
            )

            var dateTimeFormatter =
                remember { LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm:ss") } }
            LaunchedEffect(formatterState) {
                val dateFormatter = if (formatterState.dateFormatter.isBlank()) "yyyy-MM-dd"
                else formatterState.dateFormatter
                val timeFormatter = if (formatterState.timeFormatter.isBlank()) "HH:mm:ss"
                else formatterState.timeFormatter
                dateTimeFormatter =
                    LocalDateTime.Format { byUnicodePattern("$dateFormatter $timeFormatter") }
            }

            var createdTime by remember { mutableStateOf("") }
            LaunchedEffect(noteEditingState.createdAt, dateTimeFormatter) {
                if (noteEditingState.createdAt.isNotBlank())
                    withContext(Dispatchers.Default) {
                        val createdInstant = Instant.parse(noteEditingState.createdAt)
                        val createdLocalDateTime =
                            createdInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                        createdTime = createdLocalDateTime.format(dateTimeFormatter)
                    }
            }

            var updatedTime by remember { mutableStateOf("") }
            LaunchedEffect(noteEditingState.updatedAt, dateTimeFormatter) {
                if (noteEditingState.updatedAt.isNotBlank())
                    withContext(Dispatchers.Default) {
                        val updatedInstant = Instant.parse(noteEditingState.updatedAt)
                        val updatedLocalDateTime =
                            updatedInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                        updatedTime = updatedLocalDateTime.format(dateTimeFormatter)
                    }
            }

            NoteSideSheetItem(
                key = stringResource(Res.string.created),
                value = createdTime
            )

            NoteSideSheetItem(
                key = stringResource(Res.string.updated),
                value = updatedTime
            )

            NoteSideSheetItem(
                key = stringResource(Res.string.char_count),
                value = textState.charCount.toString()
            )
            NoteSideSheetItem(
                key = stringResource(Res.string.word_count),
                value = textState.wordCountWithPunctuation.toString()
            )
            NoteSideSheetItem(
                key = stringResource(Res.string.word_count_without_punctuation),
                value = textState.wordCountWithoutPunctuation.toString()
            )
            NoteSideSheetItem(
                key = stringResource(Res.string.line_count),
                value = textState.lineCount.toString()
            )
            NoteSideSheetItem(
                key = stringResource(Res.string.paragraph_count),
                value = textState.paragraphCount.toString()
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
}
