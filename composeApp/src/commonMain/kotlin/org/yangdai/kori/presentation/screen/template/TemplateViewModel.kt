package org.yangdai.kori.presentation.screen.template

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kmark.MarkdownElementTypes
import kmark.ast.ASTNode
import kmark.ast.getTextInNode
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.html.HtmlGenerator
import kmark.parser.MarkdownParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.domain.repository.NoteRepository
import org.yangdai.kori.presentation.component.note.HeaderNode
import org.yangdai.kori.presentation.component.note.markdown.Properties.getPropertiesLineRange
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.navigation.UiEvent
import org.yangdai.kori.presentation.screen.note.NoteEditingState
import org.yangdai.kori.presentation.screen.note.TextState
import org.yangdai.kori.presentation.screen.settings.EditorPaneState
import org.yangdai.kori.presentation.util.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
class TemplateViewModel(
    savedStateHandle: SavedStateHandle,
    dataStoreRepository: DataStoreRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Screen.Template>()

    private val _noteEditingState = MutableStateFlow(NoteEditingState())
    val noteEditingState = _noteEditingState.asStateFlow()

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    // 笔记状态
    val titleState = TextFieldState()
    val contentState = TextFieldState()
    val contentSnapshotFlow = snapshotFlow { contentState.text }
    val flavor = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavor)
    var oNote = NoteEntity(isTemplate = true)

    init {
        viewModelScope.launch {
            if (route.id.isEmpty()) {
                val currentTime = Clock.System.now().toString()
                _noteEditingState.update {
                    it.copy(
                        id = route.id,
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        isTemplate = true,
                        noteType = NoteType.entries[route.noteType]
                    )
                }
            } else
                noteRepository.getNoteById(route.id)?.let { note ->
                    titleState.setTextAndPlaceCursorAtEnd(note.title)
                    contentState.setTextAndPlaceCursorAtEnd(note.content)
                    _noteEditingState.update {
                        it.copy(
                            id = note.id,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            isPinned = note.isPinned,
                            isDeleted = note.isDeleted,
                            folderId = note.folderId,
                            noteType = note.noteType,
                            isTemplate = note.isTemplate
                        )
                    }
                    oNote = note
                }
        }
    }

    val editorState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_MARKDOWN_LINT_ENABLED)
    ) { showLineNumber, isMarkdownLintEnabled ->
        EditorPaneState(
            showLineNumber = showLineNumber,
            isMarkdownLintEnabled = isMarkdownLintEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), EditorPaneState())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val textState = contentSnapshotFlow.debounce(100).distinctUntilChanged()
        .mapLatest { TextState.fromText(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000L),
            initialValue = TextState()
        )

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

    fun updateNoteType(noteType: NoteType) {
        _noteEditingState.update {
            it.copy(noteType = noteType)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveOrUpdateNote() {
        if (_noteEditingState.value.isDeleted) return
        viewModelScope.launch {
            val noteEntity = NoteEntity(
                id = _noteEditingState.value.id,
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                folderId = null,
                createdAt = _noteEditingState.value.createdAt,
                updatedAt = Clock.System.now().toString(),
                isDeleted = false,
                isTemplate = true,
                isPinned = false,
                noteType = _noteEditingState.value.noteType
            )
            if (noteEntity.id.isEmpty() && (noteEntity.title.isNotBlank() || noteEntity.content.isNotBlank())) {
                val id = Uuid.random().toString()
                noteRepository.insertNote(noteEntity.copy(id = id))
                _noteEditingState.update { it.copy(id = id) }
            } else {
                if (oNote.title != noteEntity.title || oNote.content != noteEntity.content ||
                    oNote.noteType != noteEntity.noteType
                ) noteRepository.updateNote(noteEntity)
            }
        }
    }

    fun deleteTemplate() {
        viewModelScope.launch {
            _noteEditingState.update { it.copy(isDeleted = true) }
            if (_noteEditingState.value.id.isNotEmpty()) {
                noteRepository.deleteNoteById(_noteEditingState.value.id)
            }
            _uiEventFlow.emit(UiEvent.NavigateUp)
        }
    }
}