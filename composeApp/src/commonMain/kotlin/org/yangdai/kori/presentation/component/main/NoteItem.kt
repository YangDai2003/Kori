package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
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
            .padding(vertical = 8.dp)
            .animateItem(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 4.dp else 2.dp),
        colors = if (isSelected) CardDefaults.outlinedCardColors() else CardDefaults.elevatedCardColors(),
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
                // 顶部行：标题和类型图标
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 置顶图标
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "已置顶",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // 标题
                    Text(
                        text = if (note.title.isNotBlank()) note.title else "无标题",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 内容预览
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

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
                            else -> "未指定"
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
