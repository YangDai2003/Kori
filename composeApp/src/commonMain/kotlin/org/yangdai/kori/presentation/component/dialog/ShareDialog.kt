package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.file
import kori.composeapp.generated.resources.share_note_as
import kori.composeapp.generated.resources.text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.presentation.component.TextOptionButton
import org.yangdai.kori.presentation.util.clickToShareFile
import org.yangdai.kori.presentation.util.clickToShareText
import org.yangdai.kori.presentation.util.clipEntryOf

@Composable
fun ShareDialog(
    noteEntity: NoteEntity,
    onDismissRequest: () -> Unit
) = AlertDialog(
    modifier = Modifier.widthIn(max = 360.dp),
    onDismissRequest = onDismissRequest,
    title = {
        val clipboard = LocalClipboard.current
        val scope = rememberCoroutineScope()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(Res.string.share_note_as))
            IconButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(clipEntryOf(noteEntity.content))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.CopyAll,
                    contentDescription = null
                )
            }
        }
    },
    text = {
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
    },
    confirmButton = {}
)
