package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.card_size
import kori.composeapp.generated.resources.clip
import kori.composeapp.generated.resources.compact
import kori.composeapp.generated.resources.default_size
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.ellipsis
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.text_overflow
import kori.composeapp.generated.resources.title_only
import kori.composeapp.generated.resources.todo_text
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.SegmentText
import org.yangdai.kori.presentation.component.SegmentedControl
import org.yangdai.kori.presentation.component.main.card.DrawingImage
import org.yangdai.kori.presentation.component.main.card.NoteItemProperties
import org.yangdai.kori.presentation.component.main.card.buildMarkdownAnnotatedString
import org.yangdai.kori.presentation.component.main.card.buildPlainTextAnnotatedString
import org.yangdai.kori.presentation.component.main.card.buildTodoAnnotatedString
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.settings.CardSize
import org.yangdai.kori.presentation.screen.settings.CardSize.Companion.toInt
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.SampleMarkdownNote
import org.yangdai.kori.presentation.util.SampleTodoNote
import org.yangdai.kori.presentation.util.formatInstant
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun CardPane(mainViewModel: MainViewModel) {

    val cardPaneState by mainViewModel.cardPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Box(Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 160.dp)
        ) {
            items(sampleNotes, key = { it.id }) { note ->
                NoteItem(
                    note = note,
                    noteItemProperties = NoteItemProperties(
                        cardSize = cardPaneState.cardSize,
                        clipOverflow = cardPaneState.clipOverflow
                    )
                )
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.4f)
            )
        ) {
            Column(
                Modifier.fillMaxWidth().padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp),
                    text = stringResource(Res.string.text_overflow),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                SegmentedControl(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    segments = listOf(
                        stringResource(Res.string.ellipsis),
                        stringResource(Res.string.clip)
                    ),
                    selectedSegmentIndex = if (cardPaneState.clipOverflow) 1 else 0,
                    onSegmentSelected = { index ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.CLIP_OVERFLOW_TEXT,
                            index == 1
                        )
                    },
                    content = { SegmentText(it) }
                )
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp),
                    text = stringResource(Res.string.card_size),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                SegmentedControl(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    segments = listOf(
                        stringResource(Res.string.default_size),
                        stringResource(Res.string.title_only),
                        stringResource(Res.string.compact)
                    ),
                    selectedSegmentIndex = cardPaneState.cardSize.toInt(),
                    onSegmentSelected = { index ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.CARD_SIZE,
                            index
                        )
                    },
                    content = { SegmentText(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
val sampleNotes = listOf(
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Sample Note - Markdown",
        content = SampleMarkdownNote,
        isPinned = true,
        noteType = NoteType.MARKDOWN
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "This is a note with a very long title, designed to test the animated marquee effect of long titles.",
        content = "",
        isPinned = false
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Sample Note - Todo.txt",
        content = SampleTodoNote,
        isPinned = false,
        noteType = NoteType.TODO
    )
)

@OptIn(ExperimentalTime::class)
@Composable
private fun LazyItemScope.NoteItem(
    note: NoteEntity,
    noteItemProperties: NoteItemProperties
) = OutlinedCard(Modifier.padding(bottom = 8.dp).animateItem()) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val title = note.title
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }

        // 内容预览
        if (noteItemProperties.cardSize != CardSize.TITLE_ONLY) {
            val content = note.content.lineSequence().take(10).joinToString("\n")
            when (note.noteType) {
                NoteType.PLAIN_TEXT ->
                    CardContentText(
                        text = buildPlainTextAnnotatedString(content),
                        noteItemProperties = noteItemProperties
                    )

                NoteType.MARKDOWN ->
                    CardContentText(
                        text = buildMarkdownAnnotatedString(content),
                        noteItemProperties = noteItemProperties
                    )

                NoteType.TODO ->
                    CardContentText(
                        text = buildTodoAnnotatedString(content),
                        noteItemProperties = noteItemProperties
                    )

                NoteType.Drawing ->
                    DrawingImage(note = note, noteItemProperties = noteItemProperties)
            }
        }

        // 底部行：笔记类型和更新时间信息
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    NoteType.Drawing -> stringResource(Res.string.drawing)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.weight(1f))

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
}

@Composable
private fun CardContentText(text: AnnotatedString, noteItemProperties: NoteItemProperties) =
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = if (noteItemProperties.cardSize == CardSize.DEFAULT) 7 else 3,
        overflow = if (noteItemProperties.clipOverflow) TextOverflow.Clip else TextOverflow.Ellipsis
    )
