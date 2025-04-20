package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType

@Composable
fun LazyGridItemScope.NoteItem(
    note: NoteEntity,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(bottom = 8.dp)
            .animateItem(),
        colors = if (isSelected) CardDefaults.outlinedCardColors()
        else CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
        elevation = if (isSelected) CardDefaults.outlinedCardElevation() else CardDefaults.cardElevation()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    modifier = Modifier.weight(1f),
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis
                )

                // 底部行：笔记类型和更新时间信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .alpha(0.7f)
                ) {

                    Text(
                        text = when (note.noteType) {
                            NoteType.PLAIN_TEXT -> "纯文本"
                            NoteType.LITE_MARKDOWN -> "Lite Markdown"
                            NoteType.STANDARD_MARKDOWN -> "Standard Markdown"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // 格式化更新时间
                    val updatedAtFormatted = try {
                        val instant = Instant.parse(note.updatedAt)
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        val month = localDateTime.monthNumber.toString().padStart(2, '0')
                        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
                        val hour = localDateTime.hour.toString().padStart(2, '0')
                        val minute = localDateTime.minute.toString().padStart(2, '0')
                        "${localDateTime.year}-$month-$day $hour:$minute"
                    } catch (_: Exception) {
                        note.updatedAt
                    }

                    Text(
                        text = updatedAtFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

@Composable
fun LazyStaggeredGridItemScope.NoteItem(
    note: NoteEntity,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateItem(),
        colors = if (isSelected) CardDefaults.outlinedCardColors()
        else CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
        elevation = if (isSelected) CardDefaults.outlinedCardElevation() else CardDefaults.cardElevation()
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
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }


                // 内容预览
                if (note.content.isNotBlank())
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )


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
                            NoteType.PLAIN_TEXT -> "纯文本"
                            NoteType.LITE_MARKDOWN -> "Lite Markdown"
                            NoteType.STANDARD_MARKDOWN -> "Standard Markdown"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // 格式化更新时间
                    val updatedAtFormatted = try {
                        val instant = Instant.parse(note.updatedAt)
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        val month = localDateTime.monthNumber.toString().padStart(2, '0')
                        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
                        val hour = localDateTime.hour.toString().padStart(2, '0')
                        val minute = localDateTime.minute.toString().padStart(2, '0')
                        "${localDateTime.year}-$month-$day $hour:$minute"
                    } catch (_: Exception) {
                        note.updatedAt
                    }

                    Text(
                        text = updatedAtFormatted,
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
}