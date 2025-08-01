package org.yangdai.kori.presentation.glance

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.yangdai.kori.MainActivity
import org.yangdai.kori.R
import org.yangdai.kori.data.getDatabaseBuilder
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.getRoomDatabase
import org.yangdai.kori.data.repository.NoteRepositoryImpl
import org.yangdai.kori.presentation.component.note.markdown.Properties.splitPropertiesAndContent

class MyAppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val database = getRoomDatabase(getDatabaseBuilder(context.applicationContext))
        val noteRepository = NoteRepositoryImpl(database.noteDao())

        provideContent {
            val notes by noteRepository.getAllNotes().collectAsState(initial = emptyList())
            Content(notes)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            val notes = listOf(
                NoteEntity(
                    id = "1",
                    title = "Sample Note 1",
                    content = "This is a sample note content with properties."
                ),
                NoteEntity(
                    id = "2",
                    title = "Sample Note 2",
                    content = "This is another sample note content with properties."
                )
            )
            Content(notes)
        }
    }

    @Composable
    private fun Content(notes: List<NoteEntity>) {
        GlanceTheme {
            Scaffold {
                LazyColumn {
                    item {
                        Spacer(GlanceModifier.height(12.dp))
                    }
                    items(notes.take(25)) {
                        Column {
                            Column(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                                    .background(GlanceTheme.colors.secondaryContainer)
                                    .appWidgetInnerCornerRadius()
                                    .padding(4.dp)
                                    .clickable(
                                        actionStartActivity<MainActivity>(
                                            actionParametersOf(destinationKey to "note/${it.id}")
                                        )
                                    )
                            ) {
                                if (it.title.isNotEmpty())
                                    Text(
                                        modifier = GlanceModifier.fillMaxWidth(),
                                        text = it.title,
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onSurface,
                                            fontSize = 16.sp
                                        ),
                                        maxLines = 1
                                    )
                                Text(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    text = it.content.splitPropertiesAndContent().second,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 3
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }
                    item {
                        Text(
                            modifier = GlanceModifier.fillMaxWidth()
                                .padding(top = 16.dp, bottom = 24.dp)
                                .clickable(actionStartActivity<MainActivity>()),
                            text = LocalContext.current.getString(R.string.view_all_notes),
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            ),
                        )
                    }
                }
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(bottom = 12.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    SquareIconButton(
                        imageProvider = ImageProvider(R.drawable.outline_note_add_24),
                        contentDescription = "Add Note",
                        onClick = actionStartActivity<MainActivity>(
                            actionParametersOf(destinationKey to "note/")
                        )
                    )
                }
            }
        }
    }
}

private val destinationKey = ActionParameters.Key<String>("KEY_DESTINATION")

fun GlanceModifier.appWidgetInnerCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        return cornerRadius(android.R.dimen.system_app_widget_inner_radius)
    }
    return cornerRadius(8.dp)
}