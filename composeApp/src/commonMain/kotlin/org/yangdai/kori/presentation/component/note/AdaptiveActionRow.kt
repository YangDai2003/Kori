package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kfile.AudioPicker
import kfile.ImagesPicker
import kfile.VideoPicker
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.control
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.dialog.TableDialog
import org.yangdai.kori.presentation.component.note.markdown.MarkdownActionRow
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextActionRow
import org.yangdai.kori.presentation.component.note.todo.TodoTextActionRow

interface ActionRowScope {

    val isTemplateActionRow: Boolean

    @Composable
    fun ActionRow(content: @Composable RowScope.() -> Unit)

    @Composable
    fun ActionRowSection(content: @Composable RowScope.() -> Unit)

    @Composable
    fun ActionButton(
        icon: ImageVector,
        hint: String = "",
        actionText: String = "",
        enabled: Boolean = true,
        onClick: () -> Unit
    )

    @Composable
    fun ActionButton(
        icon: Painter,
        hint: String = "",
        actionText: String = "",
        enabled: Boolean = true,
        onClick: () -> Unit
    )
}

class ActionRowScopeImpl(
    val showElevation: Boolean,
    val showAIAssistPlaceholder: Boolean,
    val isTemplate: Boolean
) : ActionRowScope {

    override val isTemplateActionRow: Boolean
        get() = isTemplate

    @Composable
    override fun ActionRow(content: @Composable RowScope.() -> Unit) =
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(48.dp)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val containerWidth by rememberUpdatedState(constraints.maxWidth)
            var contentWidth by remember { mutableStateOf(0) }
            val startPadding by remember(showAIAssistPlaceholder) {
                derivedStateOf {
                    if (showAIAssistPlaceholder) {
                        val availableWidth = containerWidth - contentWidth
                        if (contentWidth <= 0 || availableWidth <= 0) 52.dp
                        else {
                            with(density) {
                                val startWidth = (availableWidth / 2f).toDp()
                                if (startWidth < 52.dp) 52.dp - startWidth
                                else 0.dp
                            }
                        }
                    } else 0.dp
                }
            }
            Row(
                modifier = Modifier
                    .onSizeChanged { contentWidth = it.width }
                    .padding(start = startPadding)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = {
                    Spacer(Modifier.width(4.dp))
                    content()
                    Spacer(Modifier.width(4.dp))
                }
            )
        }

    @Composable
    override fun ActionRowSection(content: @Composable RowScope.() -> Unit) {
        val color by animateColorAsState(
            targetValue = if (showElevation) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow,
            label = "ActionRowSectionColorAnimation"
        )
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .focusProperties { canFocus = false },
            shape = MaterialTheme.shapes.medium,
            color = color
        ) {
            Row {
                content()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ActionButton(
        icon: ImageVector,
        hint: String,
        actionText: String,
        enabled: Boolean,
        onClick: () -> Unit
    ) = TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            if (actionText.isEmpty() && hint.isEmpty()) return@TooltipBox
            PlainTooltip {
                val platformKeyboardShortCut = when (currentPlatformInfo.operatingSystem) {
                    OS.MACOS, OS.IOS -> "⌘"
                    else -> stringResource(Res.string.control)
                }
                val annotatedString = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(hint)
                    }
                    if (hint.isNotEmpty() && actionText.isNotEmpty()) append("\n")
                    if (actionText.isNotEmpty()) append("$platformKeyboardShortCut + $actionText")
                }
                Text(annotatedString, textAlign = TextAlign.Center)
            }
        },
        state = rememberTooltipState(),
        enableUserInput = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().aspectRatio(1f).pointerHoverIcon(PointerIcon.Hand)
                .clickable(enabled = enabled, role = Role.Button) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f),
                contentDescription = null
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ActionButton(
        icon: Painter,
        hint: String,
        actionText: String,
        enabled: Boolean,
        onClick: () -> Unit
    ) = TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            if (actionText.isEmpty() && hint.isEmpty()) return@TooltipBox
            PlainTooltip {
                val platformKeyboardShortCut = when (currentPlatformInfo.operatingSystem) {
                    OS.MACOS, OS.IOS -> "⌘"
                    else -> stringResource(Res.string.control)
                }
                val annotatedString = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(hint)
                    }
                    if (hint.isNotEmpty() && actionText.isNotEmpty()) append("\n")
                    if (actionText.isNotEmpty()) append("$platformKeyboardShortCut + $actionText")
                }
                Text(annotatedString, textAlign = TextAlign.Center)
            }
        },
        state = rememberTooltipState(),
        enableUserInput = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().aspectRatio(1f).pointerHoverIcon(PointerIcon.Hand)
                .clickable(enabled = enabled, role = Role.Button) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f),
                contentDescription = null
            )
        }
    }
}

/**
 * A composable function that displays a row of actions, adapting its content based on the note type.
 * This row typically appears at the bottom of a note editor, providing context-sensitive controls
 * like text formatting for Markdown, or task-related actions for a to-do list.
 * The row's appearance, such as its elevation, changes based on the scroll state.
 *
 * @param visible Controls the visibility of the action row.
 * @param noteType The [NoteType] of the note being edited, which determines which set of actions to display.
 * @param scrollState The [ScrollState] of the associated editor content. Used to determine if elevation should be shown.
 * @param textFieldState The [TextFieldState] of the editor, passed to the specific action row implementations.
 * @param isTemplate A flag indicating if the note is a template, which might alter the available actions.
 * @param onEditorRowAction A callback lambda that is invoked when an action within the row is triggered.
 */
@Composable
private fun AdaptiveActionRowLayout(
    visible: Boolean,
    noteType: NoteType,
    scrollState: ScrollState,
    textFieldState: TextFieldState,
    showAIAssistPlaceholder: Boolean,
    isTemplate: Boolean,
    onEditorRowAction: (Action) -> Unit
) {
    val showElevation by remember(visible) {
        derivedStateOf {
            scrollState.canScrollForward && visible
        }
    }
    val color by animateColorAsState(
        targetValue = if (showElevation) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surface,
        label = "ActionRowColorAnimation"
    )

    val scope = remember(showElevation, showAIAssistPlaceholder, isTemplate) {
        ActionRowScopeImpl(showElevation, showAIAssistPlaceholder, isTemplate)
    }

    Column(Modifier.fillMaxWidth().background(color).navigationBarsPadding()) {
        AnimatedVisibility(visible) {
            with(scope) {
                when (noteType) {
                    NoteType.PLAIN_TEXT -> PlainTextActionRow(textFieldState, onEditorRowAction)

                    NoteType.MARKDOWN -> MarkdownActionRow(textFieldState, onEditorRowAction)

                    NoteType.TODO -> TodoTextActionRow(textFieldState, onEditorRowAction)

                    NoteType.Drawing -> {
                        // 绘图不需要编辑栏
                    }
                }
            }
        }
    }
}

@Composable
fun AdaptiveActionRow(
    visible: Boolean,
    noteType: NoteType,
    noteId: String,
    scrollState: ScrollState,
    contentState: TextFieldState,
    showAIAssistPlaceholder: Boolean,
    isTemplate: Boolean = false,
    onTemplatesAction: () -> Unit = {}
) {

    var showImagesPicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var showAudioPicker by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }

    AdaptiveActionRowLayout(
        visible = visible,
        noteType = noteType,
        scrollState = scrollState,
        textFieldState = contentState,
        showAIAssistPlaceholder = showAIAssistPlaceholder && noteType != NoteType.PLAIN_TEXT,
        isTemplate = isTemplate
    ) { action ->
        when (action) {
            Action.Templates -> onTemplatesAction()
            Action.Images -> showImagesPicker = true
            Action.Video -> showVideoPicker = true
            Action.Audio -> showAudioPicker = true
            Action.Table -> showTableDialog = true
        }
    }

    if (showTableDialog) {
        TableDialog(
            onDismissRequest = { showTableDialog = false },
            onConfirm = { rows, columns ->
                contentState.edit { table(rows, columns) }
                showTableDialog = false
            }
        )
    }

    if (showImagesPicker) {
        ImagesPicker(noteId) {
            if (it.isNotEmpty()) contentState.edit { addImageLinks(it) }
            showImagesPicker = false
        }
    }

    if (showVideoPicker) {
        VideoPicker(noteId) {
            if (it != null) contentState.edit { addVideoLink(it) }
            showVideoPicker = false
        }
    }

    if (showAudioPicker) {
        AudioPicker(noteId) {
            if (it != null) contentState.edit { addAudioLink(it) }
            showAudioPicker = false
        }
    }
}

sealed class Action {
    object Images : Action()
    object Video : Action()
    object Audio : Action()
    object Templates : Action()
    object Table : Action()
}