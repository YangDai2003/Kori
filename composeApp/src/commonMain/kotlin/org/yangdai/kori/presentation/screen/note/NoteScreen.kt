package org.yangdai.kori.presentation.screen.note

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.created
import kori.composeapp.generated.resources.delete
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.dialog.NoteTypeDialog
import org.yangdai.kori.presentation.component.note.FindAndReplaceField
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.NoteSideSheet
import org.yangdai.kori.presentation.component.note.NoteSideSheetItem
import org.yangdai.kori.presentation.component.note.StandardTextField
import org.yangdai.kori.presentation.event.UiEvent
import org.yangdai.kori.presentation.navigation.Screen
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel = koinViewModel(),
    noteId: String,
    navigateToScreen: (Screen) -> Unit,
    navigateUp: () -> Unit
) {

    val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsStateWithLifecycle()
    val noteEditingState by viewModel.noteEditingState.collectAsStateWithLifecycle()
    val textState by viewModel.textState.collectAsStateWithLifecycle()

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

    var isReadView by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
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

                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = { isReadView = !isReadView }) {
                        Icon(
                            imageVector = if (isReadView) Icons.Outlined.EditNote
                            else Icons.AutoMirrored.Outlined.MenuBook,
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
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
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
                                    style = MaterialTheme.typography.headlineLarge.copy(
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
            }

            val scrollState = rememberScrollState()
            StandardTextField(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = viewModel.contentState,
                scrollState = scrollState,
                readMode = isReadView,
                showLineNumbers = true,
                isLintActive = true,
                headerRange = selectedHeader,
                findAndReplaceState = findAndReplaceState,
                onFindAndReplaceUpdate = { findAndReplaceState = it },
            )
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
            NoteSideSheetItem(
                key = stringResource(Res.string.created),
                value = noteEditingState.createdAt
            )
            NoteSideSheetItem(
                key = stringResource(Res.string.updated),
                value = noteEditingState.updatedAt
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
