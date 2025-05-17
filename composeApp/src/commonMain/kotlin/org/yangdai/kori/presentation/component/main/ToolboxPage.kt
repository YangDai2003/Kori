package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kfile.getPath
import knet.DocumentService
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.add_sample_note
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.confirm
import kori.composeapp.generated.resources.edit_local_file
import kori.composeapp.generated.resources.fetch_file_from_url
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.dialog.FilePickerDialog
import org.yangdai.kori.presentation.navigation.Screen

@Composable
fun ToolboxPage(navigateToScreen: (Screen) -> Unit, addSampleNote: () -> Unit) {

    var showFilePickerDialog by remember { mutableStateOf(false) }
    var showURLDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val documentService = remember { DocumentService() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(Modifier.widthIn(max = 600.dp).padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(16.dp))

            ListItem(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(CardDefaults.shape)
                    .clickable { addSampleNote() },
                headlineContent = {
                    Text(stringResource(Res.string.add_sample_note))
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = null
                    )
                }
            )

            ListItem(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(CardDefaults.shape)
                    .clickable { showFilePickerDialog = true },
                headlineContent = {
                    Text(stringResource(Res.string.edit_local_file))
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = null
                    )
                }
            )

            ListItem(
                modifier = Modifier.clip(CardDefaults.shape)
                    .clickable { showURLDialog = true },
                headlineContent = {
                    Text(stringResource(Res.string.fetch_file_from_url))
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = null
                    )
                }
            )
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
            onDismissRequest = { showURLDialog = false },
            text = {
                Column {
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            isError = false
                        },
                        label = { Text("URL") },
                        isError = isError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SelectionContainer {
                        Column {
                            var showMoreLinkHint by remember { mutableStateOf(false) }
                            LinkHintText(
                                "Github",
                                "https://raw.githubusercontent.com/<username>/<repository>/<branch>/path/to/your/file.md",
                                modifier = Modifier.clickable {
                                    showMoreLinkHint = !showMoreLinkHint
                                }
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
                            Text(
                                "...",
                                modifier = Modifier.clickable {
                                    showMoreLinkHint = !showMoreLinkHint
                                })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
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
                                        sharedContentText = document
                                    )
                                )
                            }.onFailure {
                                isError = true
                            }
                        }
                    }
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showURLDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun LinkHintText(name: String, url: String, modifier: Modifier = Modifier) {
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
}