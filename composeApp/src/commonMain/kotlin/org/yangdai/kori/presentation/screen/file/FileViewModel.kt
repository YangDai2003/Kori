package org.yangdai.kori.presentation.screen.file

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kfile.PlatformFile
import kfile.delete
import kfile.getExtension
import kfile.getFileName
import kfile.getLastModified
import kfile.readText
import kfile.writeText
import kmark.MarkdownElementTypes
import kmark.ast.ASTNode
import kmark.ast.getTextInNode
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.html.HtmlGenerator
import kmark.parser.MarkdownParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.markdown.Properties.getPropertiesLineRange
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.note.TextState
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.screen.settings.TemplatePaneState
import org.yangdai.kori.presentation.util.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
class FileViewModel(
    private val noteRepository: NoteRepository,
    dataStoreRepository: DataStoreRepository
) : ViewModel() {
    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    private val contentSnapshotFlow = snapshotFlow { contentState.text }
    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    val formatterState = combine(
        dataStoreRepository.stringFlow(Constants.Preferences.DATE_FORMATTER),
        dataStoreRepository.stringFlow(Constants.Preferences.TIME_FORMATTER)
    ) { dateFormatter, timeFormatter ->
        TemplatePaneState(
            dateFormatter = dateFormatter,
            timeFormatter = timeFormatter
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), TemplatePaneState())

    val templates = noteRepository.getAllTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val editorState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_MARKDOWN_LINT_ENABLED),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_DEFAULT_READING_VIEW)
    ) { showLineNumber, isMarkdownLintEnabled, isDefaultReadingView ->
        EditorPaneState(
            showLineNumber = showLineNumber,
            isMarkdownLintEnabled = isMarkdownLintEnabled,
            isDefaultReadingView = isDefaultReadingView
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), EditorPaneState())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val textState = contentSnapshotFlow.debounce(100).distinctUntilChanged()
        .mapLatest { TextState.Companion.fromText(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = TextState()
        )

    private val _fileEditingState = MutableStateFlow(FileEditingState())
    val fileEditingState = _fileEditingState.asStateFlow()

    val flavor = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavor)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val contentAndTreeFlow = contentSnapshotFlow.debounce(100).distinctUntilChanged()
        .mapLatest { content ->
            val text = content.toString()
            val tree = parser.buildMarkdownTreeFromString(text)
            text to tree
        }
        .flowOn(Dispatchers.Default)


    @OptIn(ExperimentalCoroutinesApi::class)
    val html = contentAndTreeFlow
        .mapLatest { (content, tree) ->
            HtmlGenerator(content, tree, flavor, true).generateHtml()
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ""
        )


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val outline = contentAndTreeFlow.debounce(1000)
        .mapLatest { (content, tree) ->
            val root = HeaderNode("", 0, IntRange.EMPTY)
            if (content.isBlank()) return@mapLatest root
            val propertiesLineRange = content.getPropertiesLineRange()
            try {
                val headerStack = mutableListOf(root)
                findHeadersRecursive(tree, content, headerStack, propertiesLineRange)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            root
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HeaderNode("", 0, IntRange.EMPTY)
        )

    /**
     * Recursively traverses the AST, finds headers, and builds the hierarchy.
     */
    private fun findHeadersRecursive(
        node: ASTNode,
        fullText: String,
        headerStack: MutableList<HeaderNode>,
        propertiesRange: IntRange?
    ) {
        // --- Check if the current node IS a header ---
        val headerLevel = when (node.type) {
            MarkdownElementTypes.ATX_1 -> 1
            MarkdownElementTypes.ATX_2 -> 2
            MarkdownElementTypes.ATX_3 -> 3
            MarkdownElementTypes.ATX_4 -> 4
            MarkdownElementTypes.ATX_5 -> 5
            MarkdownElementTypes.ATX_6 -> 6
            else -> 0 // Not a header type we are processing
        }
        if (headerLevel > 0) {
            val range = IntRange(node.startOffset, node.endOffset - 1)
            // --- Skip if inside properties range ---
            if (propertiesRange == null || !propertiesRange.contains(range.first)) {
                val title =
                    node.getTextInNode(fullText).trim().dropWhile { it == '#' }.trim().toString()
                val headerNode = HeaderNode(title, headerLevel, range)
                // --- Manage Hierarchy ---
                // Pop stack until parent level is less than current level
                while (headerStack.last().level >= headerLevel && headerStack.size > 1) {
                    headerStack.removeAt(headerStack.lastIndex)
                }
                // Add new header as child of the correct parent
                headerStack.last().children.add(headerNode)
                // Push new header onto stack to be the parent for subsequent deeper headers
                headerStack.add(headerNode)
            }
            return // Stop descent once a header is processed
        }
        // --- If not a header, recurse into children ---
        node.children.forEach { child ->
            findHeadersRecursive(child, fullText, headerStack, propertiesRange)
        }
    }

    private val _initialContent = MutableStateFlow("")
    val needSave = combine(
        _initialContent,
        contentSnapshotFlow
    ) { initial, current -> initial != current.toString() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            false
        )

    @OptIn(ExperimentalFoundationApi::class)
    fun loadFile(file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val title = file.getFileName()
            val content = file.readText()
            val noteType = if (file.getExtension().lowercase() in listOf(
                    "md",
                    "markdown",
                    "mkd",
                    "mdwn",
                    "mdown",
                    "mdtxt",
                    "mdtext",
                    "html"
                )
            ) NoteType.MARKDOWN else NoteType.PLAIN_TEXT
            titleState.setTextAndPlaceCursorAtEnd(title)
            contentState.setTextAndPlaceCursorAtEnd(content)
            titleState.undoState.clearHistory()
            contentState.undoState.clearHistory()
            // 记录初始内容
            _initialContent.value = content
            val updatedAt = file.getLastModified().toString()
            _fileEditingState.update {
                it.copy(
                    updatedAt = updatedAt,
                    fileType = noteType
                )
            }
        }
    }

    fun saveFile(file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = contentState.text.toString()
            file.writeText(content)
            _initialContent.value = content
        }
    }

    fun deleteFile(file: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.delete())
                _uiEventChannel.send(UiEvent.NavigateUp)
        }
    }

    fun updateFileType(noteType: NoteType) {
        _fileEditingState.update {
            it.copy(fileType = noteType)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveNoteAsTemplate() {
        viewModelScope.launch {
            val currentTime = Clock.System.now().toString()
            val noteEntity = NoteEntity(
                id = Uuid.random().toString(),
                title = titleState.text.toString()
                    .ifBlank {
                        "Template " + Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(LocalDateTime.Formats.ISO)
                    },
                content = contentState.text.toString(),
                folderId = null,
                createdAt = currentTime,
                updatedAt = currentTime,
                isDeleted = false,
                isTemplate = true,
                isPinned = false,
                noteType = _fileEditingState.value.fileType
            )
            noteRepository.insertNote(noteEntity)
        }
    }
}