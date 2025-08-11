package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kfile.getPath
import knet.DocumentService
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.add_sample_note
import kori.composeapp.generated.resources.edit_local_file
import kori.composeapp.generated.resources.fetch_file_from_url
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.todo_text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.dialog.ConfirmButton
import org.yangdai.kori.presentation.component.dialog.DismissButton
import org.yangdai.kori.presentation.component.dialog.FilePickerDialog
import org.yangdai.kori.presentation.component.dialog.dialogShape
import org.yangdai.kori.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolboxPage(navigateToScreen: (Screen) -> Unit, addSampleNote: (NoteType) -> Unit) {

    var showFilePickerDialog by remember { mutableStateOf(false) }
    var showURLDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val documentService = remember { DocumentService() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.widthIn(max = 600.dp).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(16.dp))

            ListItem(
                modifier = Modifier.padding(bottom = 8.dp).clip(CardDefaults.shape),
                headlineContent = { Text(stringResource(Res.string.add_sample_note)) },
                trailingContent = {
                    var checked by remember { mutableStateOf(false) }
                    var type by remember { mutableStateOf(NoteType.MARKDOWN) }
                    val size = SplitButtonDefaults.ExtraSmallContainerHeight
                    SplitButtonLayout(
                        leadingButton = {
                            SplitButtonDefaults.TonalLeadingButton(
                                modifier = Modifier.heightIn(size),
                                shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
                                contentPadding = SplitButtonDefaults
                                    .leadingButtonContentPaddingFor(size),
                                onClick = { addSampleNote(type) }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.NoteAdd,
                                    modifier = Modifier.size(
                                        SplitButtonDefaults.leadingButtonIconSizeFor(size)
                                    ),
                                    contentDescription = null
                                )
                                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
                                Text(
                                    when (type) {
                                        NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                                        NoteType.TODO -> stringResource(Res.string.todo_text)
                                        else -> ""
                                    },
                                    style = ButtonDefaults.textStyleFor(size)
                                )
                            }
                        },
                        trailingButton = {
                            SplitButtonDefaults.TonalTrailingButton(
                                checked = checked,
                                onCheckedChange = { checked = it },
                                modifier =
                                    Modifier.heightIn(size).semantics {
                                        stateDescription = if (checked) "Expanded" else "Collapsed"
                                        contentDescription = "Toggle Button"
                                    },
                                shapes = SplitButtonDefaults.trailingButtonShapesFor(size),
                                contentPadding = SplitButtonDefaults
                                    .trailingButtonContentPaddingFor(size),
                            ) {
                                val rotation: Float by animateFloatAsState(if (checked) 180f else 0f)
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    modifier =
                                        Modifier
                                            .size(SplitButtonDefaults.trailingButtonIconSizeFor(size))
                                            .graphicsLayer { this.rotationZ = rotation },
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = checked,
                        shape = MaterialTheme.shapes.small,
                        offset = DpOffset(x = 48.dp, y = 0.dp),
                        onDismissRequest = { checked = false }
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier.background(
                                color = if (type == NoteType.MARKDOWN) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent
                            ),
                            text = { Text(stringResource(Res.string.markdown)) },
                            onClick = {
                                type = NoteType.MARKDOWN
                                checked = false
                            }
                        )
                        DropdownMenuItem(
                            modifier = Modifier.background(
                                color = if (type == NoteType.TODO) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent
                            ),
                            text = { Text(stringResource(Res.string.todo_text)) },
                            onClick = {
                                type = NoteType.TODO
                                checked = false
                            }
                        )
                    }
                }
            )

            ListItem(
                modifier = Modifier.padding(bottom = 8.dp).clip(CardDefaults.shape)
                    .clickable { showFilePickerDialog = true },
                headlineContent = { Text(stringResource(Res.string.edit_local_file)) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = null
                    )
                }
            )

            ListItem(
                modifier = Modifier.padding(bottom = 8.dp).clip(CardDefaults.shape)
                    .clickable { showURLDialog = true },
                headlineContent = { Text(stringResource(Res.string.fetch_file_from_url)) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = null
                    )
                }
            )

            WidgetListItem()

            Spacer(Modifier.navigationBarsPadding())
        }
    }

    if (showFilePickerDialog)
        FilePickerDialog { pickedFile ->
            showFilePickerDialog = false
            pickedFile?.let {
                navigateToScreen(Screen.File(pickedFile.getPath()))
            }
        }

    if (showURLDialog) {
        var url by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        AlertDialog(
            shape = dialogShape(),
            onDismissRequest = { showURLDialog = false },
            text = {
                Column {
                    TextField(
                        value = url,
                        onValueChange = {
                            url = it
                            isError = false
                        },
                        label = { Text("URL") },
                        isError = isError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                    SelectionContainer {
                        Column {
                            var showMoreLinkHint by remember { mutableStateOf(false) }
                            LinkHintText(
                                "Github",
                                "https://raw.githubusercontent.com/<username>/<repository>/<branch>/path/to/your/file.md",
                                Modifier.clickable { showMoreLinkHint = !showMoreLinkHint }
                            )
                            AnimatedVisibility(showMoreLinkHint) {
                                Column {
                                    LinkHintText(
                                        "Gist",
                                        "https://gist.githubusercontent.com/<username>/<gist_id>/raw/<optional_commit_hash>/<filename>"
                                    )
                                    LinkHintText(
                                        "Gitlab",
                                        "https://gitlab.com/<username>/<repository_name>/-/raw/<branch_name>/<path_to_file>?ref_type=heads"
                                    )
                                    LinkHintText(
                                        "Bitbucket",
                                        "https://bitbucket.org/<username>/<repository_name>/raw/<branch_name>/<path_to_file>"
                                    )
                                }
                            }
                            Text("...", Modifier.clickable { showMoreLinkHint = !showMoreLinkHint })
                        }
                    }
                }
            },
            confirmButton = {
                ConfirmButton {
                    coroutineScope.launch {
                        val result = documentService.fetchNetDocument(url)
                        result.onSuccess { document ->
                            showURLDialog = false
                            navigateToScreen(
                                Screen.Note(
                                    sharedContentTitle = Regex(
                                        """([^/?#]+\.md|txt|json|markdown)""",
                                        RegexOption.IGNORE_CASE
                                    )
                                        .find(url)?.value
                                        ?: url.substringAfterLast('/').substringBefore('?')
                                            .substringBefore('#').takeIf { it.isNotBlank() }
                                        ?: url,
                                    sharedContentText = document,
                                    noteType = if (url.contains(".md", ignoreCase = true)
                                        || url.contains("markdown", ignoreCase = true)
                                    ) NoteType.MARKDOWN.ordinal else NoteType.PLAIN_TEXT.ordinal,
                                )
                            )
                        }.onFailure {
                            isError = true
                        }
                    }
                }
            },
            dismissButton = { DismissButton { showURLDialog = false } }
        )
    }
}

@Composable
expect fun WidgetListItem()

@Composable
private fun LinkHintText(name: String, url: String, modifier: Modifier = Modifier) =
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(MaterialTheme.typography.labelMedium.toSpanStyle()) {
                append("$name: ")
            }
            withStyle(
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.8f
                    )
                ).toSpanStyle()
            ) {
                append(url)
            }
        }
    )