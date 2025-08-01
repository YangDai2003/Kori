package org.yangdai.kori.presentation.glance

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.appwidget.PreviewSizeMode
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

    companion object { // assume the following specifications: A grid cell is 30 dp wide and 50 dp tall.
        private val SMALL = DpSize(60.dp, 50.dp)
        private val MEDIUM = DpSize(120.dp, 100.dp)
        private val LARGE = DpSize(150.dp, 200.dp)
    }

    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

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
                ),
                NoteEntity(
                    id = "3",
                    title = "Sample Note 3",
                    content = "This is a third sample note content with properties."
                )
            )
            Content(notes)
        }
    }

    @Composable
    private fun Content(notes: List<NoteEntity>) {
        val size = LocalSize.current
        GlanceTheme {
            if (size.height >= LARGE.height) {
                Scaffold(
                    titleBar = {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth()
                                .padding(bottom = 8.dp, top = 12.dp)
                                .padding(horizontal = 12.dp),
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
                    }
                ) {
                    NoteList(notes)
                }
            } else if (size.height >= MEDIUM.height) {
                Scaffold {
                    NoteList(notes)
                    Box(
                        modifier = GlanceModifier.fillMaxSize().padding(bottom = 12.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
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
        val spacerHeight =
            if (size.width >= MEDIUM.width && size.height < LARGE.height) 12.dp else 0.dp
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
                            .appWidgetInnerCornerRadius(12.dp)
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