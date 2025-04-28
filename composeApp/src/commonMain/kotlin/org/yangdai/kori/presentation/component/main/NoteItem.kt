@file:OptIn(FormatStringsInDatetimeFormats::class)

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
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.screen.settings.CardSize

data class NoteItemProperties(
    val showCreatedTime: Boolean = false,
    val cardSize: CardSize = CardSize.DEFAULT,
    val clipOverflow: Boolean = false
)

@Composable
private fun NoteItemCard(
    modifier: Modifier,
    isSelected: Boolean,
    content: @Composable () -> Unit
) = Surface(
    modifier = modifier,
    shape = CardDefaults.shape,
    border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
    content = content
)

@Composable
fun LazyGridItemScope.NoteItem(
    note: NoteEntity,
    noteItemProperties: NoteItemProperties,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    NoteItemCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .animateItem(),
        isSelected = isSelected
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
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (noteItemProperties.cardSize != CardSize.TITLE_ONLY) {
                    val lines = if (noteItemProperties.cardSize == CardSize.DEFAULT) 5 else 2
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        minLines = lines,
                        maxLines = lines,
                        overflow = if (noteItemProperties.clipOverflow) TextOverflow.Clip else TextOverflow.Ellipsis
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
                            NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                            NoteType.LITE_MARKDOWN -> stringResource(Res.string.markdown) + " (Lite)"
                            NoteType.STANDARD_MARKDOWN -> stringResource(Res.string.markdown) + " (Standard)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    var dateTimeFormatter =
                        remember { LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") } }

                    // 格式化更新时间
                    val updatedAtFormatted = try {
                        val instant =
                            if (noteItemProperties.showCreatedTime) Instant.parse(note.createdAt)
                            else Instant.parse(note.updatedAt)
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        localDateTime.format(dateTimeFormatter)
                    } catch (_: Exception) {
                        if (noteItemProperties.showCreatedTime) note.createdAt else note.updatedAt
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
    noteItemProperties: NoteItemProperties,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    NoteItemCard(
        modifier = Modifier.fillMaxWidth().animateItem(),
        isSelected = isSelected
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

                    if (!note.isTemplate)
                        Text(
                            text = when (note.noteType) {
                                NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                                NoteType.LITE_MARKDOWN -> stringResource(Res.string.markdown) + " (Lite)"
                                NoteType.STANDARD_MARKDOWN -> stringResource(Res.string.markdown) + " (Standard)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                    Spacer(modifier = Modifier.weight(1f))

                    var dateTimeFormatter =
                        remember { LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") } }

                    // 格式化更新时间
                    val updatedAtFormatted = try {
                        val instant =
                            if (noteItemProperties.showCreatedTime) Instant.parse(note.createdAt)
                            else Instant.parse(note.updatedAt)
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        localDateTime.format(dateTimeFormatter)
                    } catch (_: Exception) {
                        if (noteItemProperties.showCreatedTime) note.createdAt else note.updatedAt
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

@Composable
fun LazyStaggeredGridItemScope.SearchResultNoteItem(
    keyword: String,
    note: NoteEntity,
    noteItemProperties: NoteItemProperties,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    NoteItemCard(
        modifier = Modifier.fillMaxWidth().animateItem(),
        isSelected = isSelected
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
                            NoteType.LITE_MARKDOWN -> stringResource(Res.string.markdown) + " (Lite)"
                            NoteType.STANDARD_MARKDOWN -> stringResource(Res.string.markdown) + " (Standard)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    var dateTimeFormatter =
                        remember { LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") } }

                    // 格式化更新时间
                    val updatedAtFormatted = try {
                        val instant =
                            if (noteItemProperties.showCreatedTime) Instant.parse(note.createdAt)
                            else Instant.parse(note.updatedAt)
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        localDateTime.format(dateTimeFormatter)
                    } catch (_: Exception) {
                        if (noteItemProperties.showCreatedTime) note.createdAt else note.updatedAt
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