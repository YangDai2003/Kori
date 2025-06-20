package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.todo_text
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.screen.settings.CardSize
import org.yangdai.kori.presentation.util.formatInstant

data class NoteItemProperties(
    val showCreatedTime: Boolean = false,
    val cardSize: CardSize = CardSize.DEFAULT,
    val clipOverflow: Boolean = false
)

@Composable
fun LazyStaggeredGridItemScope.NoteItem(
    keyword: String,
    note: NoteEntity,
    noteItemProperties: NoteItemProperties,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) = Surface(
    modifier = Modifier.fillMaxWidth().animateItem(),
    shape = CardDefaults.shape,
    border = if (isSelected) CardDefaults.outlinedCardBorder() else null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            if (note.title.isNotBlank()) {
                val annotatedString = buildAnnotatedString {
                    val title = note.title
                    if (keyword.isEmpty()) {
                        append(title)
                        return@buildAnnotatedString
                    }
                    val index = title.indexOf(keyword, ignoreCase = true)
                    if (index != -1) {
                        append(title.substring(0, index))
                        withStyle(
                            style = SpanStyle(
                                background = Color.Cyan.copy(alpha = 0.5f),
                            )
                        ) {
                            append(title.substring(index, index + keyword.length))
                        }
                        append(title.substring(index + keyword.length))
                    } else {
                        append(title)
                    }
                }
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 内容预览
            if (noteItemProperties.cardSize != CardSize.TITLE_ONLY) {
                val lines = if (noteItemProperties.cardSize == CardSize.DEFAULT) 5 else 2
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = lines,
                    overflow = if (noteItemProperties.clipOverflow) TextOverflow.Clip else TextOverflow.Ellipsis
                )
            }

            // 底部行：笔记类型和更新时间信息
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {

                if (note.isPinned)
                    Icon(
                        modifier = Modifier.padding(end = 8.dp).size(16.dp),
                        imageVector = Icons.Default.PushPin,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                    )

                Text(
                    text = when (note.noteType) {
                        NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                        NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                        NoteType.TODO -> stringResource(Res.string.todo_text)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                val instant =
                    if (noteItemProperties.showCreatedTime) Instant.parse(note.createdAt)
                    else Instant.parse(note.updatedAt)
                Text(
                    text = formatInstant(instant),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }

        // 选择框
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}