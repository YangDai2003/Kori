package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kfile.PlatformFilePicker
import kfile.getPath
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.add_sample_note
import kori.composeapp.generated.resources.edit_local_file
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.todo_text
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolboxPage(navigateToScreen: (Screen) -> Unit, addSampleNote: (NoteType) -> Unit) {

    var showFilePickerDialog by remember { mutableStateOf(false) }

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
                colors = ListItemDefaults.colors(containerColor = CardDefaults.elevatedCardColors().containerColor),
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
                        onDismissRequest = { checked = false },
                        offset = DpOffset(x = 48.dp, y = 0.dp),
                        shape = MenuDefaults.standaloneGroupShape,
                        containerColor = MenuDefaults.groupStandardContainerColor
                    ) {
                        DropdownMenuItem(
                            selected = type == NoteType.MARKDOWN,
                            shapes = MenuDefaults.itemShape(0, 2),
                            text = { Text(stringResource(Res.string.markdown)) },
                            checkedLeadingIcon = { Icon(Icons.Filled.Check, null) },
                            onClick = {
                                type = NoteType.MARKDOWN
                                checked = false
                            }
                        )
                        DropdownMenuItem(
                            selected = type == NoteType.TODO,
                            shapes = MenuDefaults.itemShape(1, 2),
                            text = { Text(stringResource(Res.string.todo_text)) },
                            checkedLeadingIcon = { Icon(Icons.Filled.Check, null) },
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
                colors = ListItemDefaults.colors(containerColor = CardDefaults.elevatedCardColors().containerColor),
                headlineContent = { Text(stringResource(Res.string.edit_local_file)) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                        contentDescription = null
                    )
                }
            )

            WidgetListItem()

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }

    if (showFilePickerDialog)
        PlatformFilePicker { pickedFile ->
            showFilePickerDialog = false
            pickedFile?.let {
                navigateToScreen(Screen.File(pickedFile.getPath()))
            }
        }
}

@Composable
expect fun WidgetListItem()