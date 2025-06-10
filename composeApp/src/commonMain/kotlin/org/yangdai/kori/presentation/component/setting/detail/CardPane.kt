package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.card_size
import kori.composeapp.generated.resources.clip
import kori.composeapp.generated.resources.compact
import kori.composeapp.generated.resources.default_size
import kori.composeapp.generated.resources.ellipsis
import kori.composeapp.generated.resources.text_overflow
import kori.composeapp.generated.resources.title_only
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.main.NoteItemProperties
import org.yangdai.kori.presentation.component.main.Page
import org.yangdai.kori.presentation.screen.settings.CardSize.Companion.toInt
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.util.Constants
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun CardPane(settingsViewModel: SettingsViewModel) {

    val cardPaneState by settingsViewModel.cardPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Box(Modifier.fillMaxSize()) {
        Page(
            notes = sampleNotes,
            contentPadding = PaddingValues(16.dp),
            noteItemProperties = NoteItemProperties(
                showCreatedTime = true,
                cardSize = cardPaneState.cardSize,
                clipOverflow = cardPaneState.clipOverflow
            ),
            navigateToNote = {},
            selectedNotes = mutableSetOf(),
            isSelectionMode = false
        )

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
            )
        ) {
            val overflowStyles = listOf(
                stringResource(Res.string.ellipsis),
                stringResource(Res.string.clip)
            )
            val sizeStyles = listOf(
                stringResource(Res.string.default_size),
                stringResource(Res.string.title_only),
                stringResource(Res.string.compact)
            )

            Column(
                Modifier.fillMaxWidth().padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.large
                    )
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = stringResource(Res.string.text_overflow),
                            maxLines = 1
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    overflowStyles.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = overflowStyles.size
                            ),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.CLIP_OVERFLOW_TEXT,
                                    index == 1
                                )
                            },
                            selected = if (cardPaneState.clipOverflow) 1 == index else 0 == index,
                            label = {
                                Text(
                                    label,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                        )
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.large
                    )
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = stringResource(Res.string.card_size),
                            maxLines = 1
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 8.dp),
                ) {
                    sizeStyles.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = sizeStyles.size
                            ),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.CARD_SIZE,
                                    index
                                )
                            },
                            selected = index == cardPaneState.cardSize.toInt(),
                            label = {
                                Text(
                                    label,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
val sampleNotes = listOf(
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Grocery List for the Week",
        content = "Don't forget to buy:\n- Milk\n- Eggs\n- Bread\n- Cheese\n- Fruits (Apples, Bananas)\n- Vegetables (Carrots, Broccoli)",
        isPinned = true,
        noteType = NoteType.MARKDOWN
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Project Ideas",
        content = "Some of the ideas for my project:\n1. Develop a mobile app that tracks daily water intake.\n2. Create a web application for managing personal finances.\n3. Build a simple game with Kotlin.\n4. Explore the possibility of creating a personal blog.",
        isPinned = true,
        noteType = NoteType.PLAIN_TEXT
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Quick Reminder",
        content = "Remember to call John about the meeting next week.",
        isPinned = false
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Thoughts on Kotlin",
        content = "Kotlin is a modern, statically typed programming language targeting the JVM, Android, Browser, etc. Kotlin is concise and safe, designed to be interoperable with Java.",
        isPinned = false,
        noteType = NoteType.MARKDOWN
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "",
        content = "A very long content here to test the ui display effect, this content is more than 200 characters.",
        isPinned = false
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "A very very very very very very very long title",
        content = "A short content.",
        isPinned = false
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "Books to read",
        content = "1. The Lord of the Rings\n2. Pride and Prejudice\n3. The Hitchhiker's Guide to the Galaxy\n4. 1984\n5. To Kill a Mockingbird",
        isPinned = true,
        noteType = NoteType.MARKDOWN
    ),
    NoteEntity(
        id = Uuid.random().toString(),
        title = "My Note",
        content = """
                This is a very long text content for testing UI display effect. 
                It contains multiple paragraphs to simulate real-world note content. 
                The content also includes lists, links, bold, and italic text for comprehensive testing.
                - item 1
                - item 2
                - item 3
                 https://www.example.com

                 **This is Bold Text**

                 *This is Italic Text*
                  More test content.
        """.trimIndent(),
        isPinned = false,
        noteType = NoteType.MARKDOWN
    )
)
