package org.yangdai.kori.presentation.component.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kfile.NoteExporter
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.export_as
import kori.composeapp.generated.resources.file
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.share_as
import kori.composeapp.generated.resources.text
import kori.composeapp.generated.resources.todo_text
import kori.composeapp.generated.resources.type
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.processMarkdown
import org.yangdai.kori.presentation.util.clickToShareFile
import org.yangdai.kori.presentation.util.clickToShareText
import org.yangdai.kori.presentation.util.clipEntryOf

@Preview
@Composable
fun ExportDialogPreview() {
    ExportDialog(
        noteEntity = NoteEntity(),
        onDismissRequest = {}
    )
}

@Preview
@Composable
fun NoteTypeDialogPreview() {
    NoteTypeDialog(
        oNoteType = NoteType.PLAIN_TEXT,
        onDismissRequest = {},
        onNoteTypeSelected = {}
    )
}

@Preview
@Composable
fun ShareDialogPreview() {
    ShareDialog(
        noteEntity = NoteEntity(),
        onDismissRequest = {}
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DialogWithoutButtons(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showDialog = true
    }

    val onDismiss = {
        scope.launch {
            showDialog = false
            delay(300L)
            onDismissRequest()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val alpha by animateFloatAsState(if (showDialog) 0.6f else 0f)
        val backgroundColor = MaterialTheme.colorScheme.scrim.copy(alpha = alpha)
        Canvas(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            }
        ) {
            drawRect(backgroundColor)
        }
        BackHandler { onDismiss() }
        AnimatedVisibility(
            visible = showDialog,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 320.dp),
                shape = dialogShape(),
                color = AlertDialogDefaults.containerColor
            ) {
                Column(Modifier.padding(24.dp)) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.headlineSmallEmphasized
                            .copy(AlertDialogDefaults.titleContentColor)
                    ) { title() }
                    Spacer(Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

enum class ExportType {
    TXT,
    MARKDOWN,
    HTML
}

@Composable
fun ExportDialog(
    noteEntity: NoteEntity,
    onDismissRequest: () -> Unit
) {
    var showSaveFileDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf(ExportType.TXT) }
    var html by rememberSaveable { mutableStateOf("") }

    DialogWithoutButtons(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.export_as)) },
        content = {
            Column(Modifier.fillMaxWidth()) {
                if (noteEntity.noteType == NoteType.MARKDOWN) {
                    TextOptionButton(buttonText = "MARKDOWN") {
                        exportType = ExportType.MARKDOWN
                        showSaveFileDialog = true
                    }

                    TextOptionButton(buttonText = "HTML") {
                        exportType = ExportType.HTML
                        showSaveFileDialog = true
                    }

                    LaunchedEffect(noteEntity.content) {
                        html = processMarkdown(noteEntity.content)
                    }
                } else {
                    TextOptionButton(buttonText = "TXT") {
                        exportType = ExportType.TXT
                        showSaveFileDialog = true
                    }
                }
            }
        }
    )

    if (showSaveFileDialog) {
        NoteExporter(exportType, noteEntity, html) {
            showSaveFileDialog = false
            onDismissRequest()
        }
    }
}

@Composable
fun NoteTypeDialog(
    oNoteType: NoteType,
    onDismissRequest: () -> Unit,
    onNoteTypeSelected: (NoteType) -> Unit
) {
    var selectedNoteType by remember { mutableStateOf(oNoteType) }
    DialogWithoutButtons(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.type)) },
        content = {
            Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                NoteType.entries.filter { it != NoteType.Drawing }.forEach { noteType ->
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .selectable(
                                selected = (selectedNoteType == noteType),
                                role = Role.RadioButton,
                                onClick = {
                                    selectedNoteType = noteType
                                    onNoteTypeSelected(noteType)
                                    onDismissRequest()
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val typeName = when (noteType) {
                            NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                            NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                            NoteType.TODO -> stringResource(Res.string.todo_text)
                            NoteType.Drawing -> stringResource(Res.string.drawing)
                        }
                        Text(
                            typeName,
                            modifier = Modifier.minimumInteractiveComponentSize()
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        RadioButton(
                            modifier = Modifier.padding(end = 16.dp),
                            selected = (selectedNoteType == noteType),
                            onClick = null
                        )
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareDialog(
    noteEntity: NoteEntity,
    onDismissRequest: () -> Unit
) = DialogWithoutButtons(
    onDismissRequest = onDismissRequest,
    title = {
        val clipboard = LocalClipboard.current
        val hapticFeedback = LocalHapticFeedback.current
        val scope = rememberCoroutineScope()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(Res.string.share_as))
            IconButton(
                modifier = Modifier.size(
                    IconButtonDefaults.extraSmallContainerSize(
                        widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform
                    )
                ),
                shape = IconButtonDefaults.extraSmallSquareShape,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    scope.launch {
                        clipboard.setClipEntry(clipEntryOf(noteEntity.content))
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                    imageVector = Icons.Outlined.CopyAll,
                    contentDescription = null
                )
            }
        }
    },
    content = {
        Column(Modifier.fillMaxWidth()) {
            TextOptionButton(
                modifier = Modifier.clickToShareText(noteEntity.content),
                buttonText = stringResource(Res.string.text)
            )

            TextOptionButton(
                modifier = Modifier.clickToShareFile(noteEntity),
                buttonText = stringResource(Res.string.file)
            )
        }
    }
)

@Composable
private fun TextOptionButton(
    modifier: Modifier = Modifier,
    buttonText: String
) = Box(
    modifier = Modifier
        .padding(top = 8.dp)
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .then(modifier),
    contentAlignment = Alignment.Center
) {
    Text(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .padding(vertical = 4.dp),
        text = buttonText,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun TextOptionButton(
    buttonText: String,
    onButtonClick: () -> Unit
) = TextButton(
    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    shape = MaterialTheme.shapes.medium,
    colors = ButtonDefaults.textButtonColors().copy(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ),
    onClick = onButtonClick
) {
    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = buttonText,
        style = MaterialTheme.typography.titleMedium
    )
}