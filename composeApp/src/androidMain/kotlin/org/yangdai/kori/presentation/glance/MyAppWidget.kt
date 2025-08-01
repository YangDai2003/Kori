package org.yangdai.kori.presentation.glance

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
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

    override val sizeMode: SizeMode = SizeMode.Exact

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
        val size = LocalSize.current
        val padding = if (size.width > 210.dp) 12.dp else 4.dp
        GlanceTheme {
            if (size.height > 300.dp) {
                Scaffold(
                    titleBar = {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth()
                                .padding(bottom = 8.dp, top = padding)
                                .padding(horizontal = padding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kori" + if (notes.isNotEmpty()) " " else "",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onBackground,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            val notesCount = if (notes.size > 99) "99+" else notes.size.toString()
                            Text(
                                text = if (notes.isNotEmpty()) "($notesCount)" else "",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onBackground,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            SquareIconButton(
                                modifier = GlanceModifier.size(48.dp),
                                imageProvider = ImageProvider(R.drawable.outline_compose_24),
                                contentDescription = "Add Note",
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note/")
                                )
                            )
                        }
                    },
                    horizontalPadding = padding
                ) {
                    NoteList(notes)
                }
            } else if (size.height > 100.dp) {
                Scaffold(horizontalPadding = padding) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().padding(bottom = padding),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        NoteList(notes)
                        CircleIconButton(
                            modifier = GlanceModifier.size(48.dp),
                            imageProvider = ImageProvider(R.drawable.outline_compose_24),
                            contentDescription = "Add Note",
                            backgroundColor = GlanceTheme.colors.primary,
                            contentColor = GlanceTheme.colors.onPrimary,
                            onClick = actionStartActivity<MainActivity>(
                                actionParametersOf(destinationKey to "note/")
                            )
                        )
                    }
                }
            } else {
                Scaffold {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kori",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        SquareIconButton(
                            modifier = GlanceModifier.size(48.dp),
                            imageProvider = ImageProvider(R.drawable.outline_compose_24),
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

    @Composable
    private fun NoteList(notes: List<NoteEntity>) {
        val size = LocalSize.current
        val spacerHeight = if (size.width > 210.dp && size.height < 300.dp) 12.dp else 0.dp
        val padding = if (size.width > 210.dp) 12.dp else 4.dp
        LazyColumn(GlanceModifier.fillMaxSize()) {
            item {
                Spacer(GlanceModifier.height(spacerHeight))
            }
            items(notes.take(25)) {
                Column {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(GlanceTheme.colors.secondaryContainer)
                            .appWidgetInnerCornerRadius(padding)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
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
                                    color = GlanceTheme.colors.onSecondaryContainer,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                maxLines = 1
                            )
                        Text(
                            modifier = GlanceModifier.fillMaxWidth(),
                            text = it.content.splitPropertiesAndContent().second,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 13.sp
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
    }
}

private val destinationKey = ActionParameters.Key<String>("KEY_DESTINATION")

@SuppressLint("LocalContextResourcesRead")
@Composable
private fun GlanceModifier.appWidgetInnerCornerRadius(widgetPadding: Dp): GlanceModifier {
    if (Build.VERSION.SDK_INT < 31) return this
    val resources = LocalContext.current.resources
    val px = resources.getDimension(android.R.dimen.system_app_widget_background_radius)
    val widgetBackgroundRadiusDpValue = px / resources.displayMetrics.density
    if (widgetBackgroundRadiusDpValue < widgetPadding.value) return this
    return this.cornerRadius(Dp(widgetBackgroundRadiusDpValue - widgetPadding.value))
}