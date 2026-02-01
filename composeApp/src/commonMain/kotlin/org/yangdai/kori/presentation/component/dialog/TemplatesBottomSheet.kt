package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.saveAsTemplate
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.todo_text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TemplatesBottomSheet(
    onDismissRequest: () -> Unit,
    templates: List<NoteEntity>,
    onSaveButtonClick: () -> Unit,
    onTemplateClick: (template: NoteEntity) -> Unit
) {
    val templatesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()
    val hideTemplatesBottomSheet: () -> Unit = {
        coroutineScope.launch {
            templatesSheetState.hide()
        }.invokeOnCompletion {
            if (!templatesSheetState.isVisible) {
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        sheetState = templatesSheetState,
        sheetGesturesEnabled = false,
        sheetMaxWidth = DialogMaxWidth,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.templates),
                    style = MaterialTheme.typography.titleMediumEmphasized,
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
                        contentDescription = "Close",
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(templates, key = { it.id }) { template ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onTemplateClick(template)
                            hideTemplatesBottomSheet()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (template.noteType) {
                            NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                            NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                            NoteType.TODO -> stringResource(Res.string.todo_text)
                            NoteType.Drawing -> stringResource(Res.string.drawing)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = template.title,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }
            item(contentType = "Button") {
                TextButton(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = onSaveButtonClick
                ) {
                    Text(stringResource(Res.string.saveAsTemplate))
                }
            }
        }
    }
}