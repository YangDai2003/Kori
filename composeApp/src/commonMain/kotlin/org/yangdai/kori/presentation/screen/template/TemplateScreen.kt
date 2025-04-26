package org.yangdai.kori.presentation.screen.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kori.composeapp.generated.resources.delete
import kori.composeapp.generated.resources.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.editor.FindAndReplaceField
import org.yangdai.kori.presentation.component.editor.FindAndReplaceState
import org.yangdai.kori.presentation.component.editor.template.TemplateEditor
import org.yangdai.kori.presentation.component.editor.template.TemplateEditorRow
import org.yangdai.kori.presentation.event.UiEvent

@OptIn(ExperimentalMaterial3Api::class, FormatStringsInDatetimeFormats::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel = koinViewModel(),
    noteId: String,
    navigateUp: () -> Unit
) {
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()

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

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        state = viewModel.titleState,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
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
                                        style = MaterialTheme.typography.titleMedium.copy(
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
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = { viewModel.deleteTemplate() }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(Res.string.delete),
                            tint = MaterialTheme.colorScheme.error
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
            Column(Modifier.fillMaxSize().weight(1f)) {
                TemplateEditor(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    state = viewModel.contentState,
                    scrollState = scrollState,
                    readMode = false,
                    showLineNumbers = editorState.showLineNumber,
                    findAndReplaceState = findAndReplaceState,
                    onFindAndReplaceUpdate = { findAndReplaceState = it }
                )
                TemplateEditorRow(viewModel.contentState)
            }
        }
    }
}
