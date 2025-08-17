package org.yangdai.kori.presentation.glance

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Dimension
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yangdai.kori.MainActivity
import org.yangdai.kori.R
import org.yangdai.kori.data.getDatabaseBuilder
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.data.local.getRoomDatabase
import org.yangdai.kori.data.repository.NoteRepositoryImpl
import org.yangdai.kori.presentation.component.note.markdown.Properties.splitPropertiesAndContent
import java.io.File

class MyAppWidget : GlanceAppWidget() {

    companion object {
        private val EXTRA_SMALL = DpSize(48.dp, 48.dp) // 仅显示创建按钮
        private val SMALL = DpSize(128.dp, 72.dp) // 显示应用名和创建按钮
        private val MEDIUM = DpSize(128.dp, 128.dp) // 无标题栏
        private val LARGE = DpSize(288.dp, 250.dp) // 显示标题栏
    }

    override val previewSizeMode: PreviewSizeMode =
        SizeMode.Responsive(setOf(EXTRA_SMALL, SMALL, MEDIUM, LARGE))

    override val sizeMode = SizeMode.Responsive(setOf(EXTRA_SMALL, SMALL, MEDIUM, LARGE))

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
                                maxLines = 1,
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
                                maxLines = 1,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onBackground,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            CircleIconButton(
                                imageProvider = ImageProvider(R.drawable.new_text_24px),
                                contentDescription = "new plain text",
                                backgroundColor = GlanceTheme.colors.widgetBackground,
                                contentColor = GlanceTheme.colors.primary,
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=0")
                                )
                            )
                            CircleIconButton(
                                imageProvider = ImageProvider(R.drawable.new_markdown_24px),
                                contentDescription = "new markdown",
                                backgroundColor = GlanceTheme.colors.widgetBackground,
                                contentColor = GlanceTheme.colors.primary,
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=1")
                                )
                            )
                            CircleIconButton(
                                imageProvider = ImageProvider(R.drawable.new_todo_24px),
                                contentDescription = "new todo",
                                backgroundColor = GlanceTheme.colors.widgetBackground,
                                contentColor = GlanceTheme.colors.primary,
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=2")
                                )
                            )
                            CircleIconButton(
                                imageProvider = ImageProvider(R.drawable.new_draw_24px),
                                contentDescription = "new drawing",
                                backgroundColor = GlanceTheme.colors.widgetBackground,
                                contentColor = GlanceTheme.colors.primary,
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=3")
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
                            imageProvider = ImageProvider(R.drawable.new_text_24px),
                            contentDescription = "new plain text",
                            backgroundColor = GlanceTheme.colors.primary,
                            contentColor = GlanceTheme.colors.onPrimary,
                            onClick = actionStartActivity<MainActivity>(
                                actionParametersOf(destinationKey to "note/")
                            )
                        )
                    }
                }
            } else {
                if (size.width < SMALL.width)
                    Scaffold(backgroundColor = GlanceTheme.colors.secondaryContainer) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize().clickable(
                                actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note/")
                                )
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.new_text_24px),
                                contentDescription = "new plain text",
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
                            )
                        }
                    }
                else
                    Scaffold {
                        Row(
                            modifier = GlanceModifier.fillMaxSize().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SquareIconButton(
                                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                                imageProvider = ImageProvider(R.drawable.new_text_24px),
                                contentDescription = "new plain text",
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=0")
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            SquareIconButton(
                                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                                imageProvider = ImageProvider(R.drawable.new_markdown_24px),
                                contentDescription = "new markdown",
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=1")
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            SquareIconButton(
                                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                                imageProvider = ImageProvider(R.drawable.new_todo_24px),
                                contentDescription = "new todo",
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=2")
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            SquareIconButton(
                                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                                imageProvider = ImageProvider(R.drawable.new_draw_24px),
                                contentDescription = "new drawing",
                                onClick = actionStartActivity<MainActivity>(
                                    actionParametersOf(destinationKey to "note?noteType=3")
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

                        if (it.noteType == NoteType.Drawing) {
                            val context = LocalContext.current
                            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                            LaunchedEffect(it.id) {
                                withContext(Dispatchers.IO) {
                                    val noteDir = File(context.filesDir, it.id)
                                    if (!noteDir.exists()) noteDir.mkdirs()
                                    val imageFile = File(noteDir, "ink.png")
                                    val request =
                                        ImageRequest.Builder(context)
                                            .data(imageFile)
                                            .size(Dimension.Pixels(600), Dimension.Pixels(300))
                                            .build()
                                    bitmap =
                                        when (val result = context.imageLoader.execute(request)) {
                                            is ErrorResult -> null
                                            is SuccessResult -> result.image.toBitmap()
                                        }
                                }
                            }
                            bitmap?.let { bm ->
                                Image(
                                    provider = ImageProvider(bm),
                                    modifier = GlanceModifier.fillMaxWidth().height(80.dp),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null
                                )
                            }
                        } else {
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
                    }
                    Spacer(GlanceModifier.height(4.dp))
                }
            }
            item {
                Text(
                    modifier = GlanceModifier.fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    text = LocalContext.current.getString(R.string.view_all_notes),
                    maxLines = 1,
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