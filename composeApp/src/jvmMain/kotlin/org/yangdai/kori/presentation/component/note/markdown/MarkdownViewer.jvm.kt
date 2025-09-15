package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kori.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.Placeholders
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.createScrollToOffsetScript
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.escaped
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.processMarkdown
import org.yangdai.kori.presentation.theme.AppConfig
import org.yangdai.kori.presentation.util.AppLockManager
import org.yangdai.kori.presentation.util.toHexColor
import java.awt.Desktop
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.net.URI
import kotlin.math.roundToInt

val jfxPanel = InteropPanel()

private object StaticUris {
    val MERMAID = Res.getUri("files/mermaid.min.js")
    val KATEX = Res.getUri("files/katex/katex.min.js")
    val KATEX_CSS = Res.getUri("files/katex/katex.min.css")
    val KATEX_RENDER = Res.getUri("files/katex/auto-render.min.js")
    val PRISM = Res.getUri("files/prism/prism.js")
    val PRISM_LIGHT_CSS = Res.getUri("files/prism/prism-theme-light.css")
    val PRISM_DARK_CSS = Res.getUri("files/prism/prism-theme-dark.css")
}

private fun String.processHtml(styles: MarkdownStyles, appConfig: AppConfig) = this
    .replace(Placeholders.TEXT_COLOR, styles.hexTextColor)
    .replace(Placeholders.BACKGROUND_COLOR, styles.backgroundColor.toHexColor())
    .replace(Placeholders.CODE_BACKGROUND, styles.hexCodeBackgroundColor)
    .replace(Placeholders.PRE_BACKGROUND, styles.hexPreBackgroundColor)
    .replace(Placeholders.QUOTE_BACKGROUND, styles.hexQuoteBackgroundColor)
    .replace(Placeholders.LINK_COLOR, styles.hexLinkColor)
    .replace(Placeholders.BORDER_COLOR, styles.hexBorderColor)
    .replace(Placeholders.COLOR_SCHEME, if (appConfig.darkMode) "dark" else "light")
    .replace(Placeholders.FONT_SCALE, "${(appConfig.fontScale * 100).roundToInt()}%")
    .replace("{{MERMAID}}", StaticUris.MERMAID)
    .replace("{{KATEX}}", StaticUris.KATEX)
    .replace("{{KATEX-CSS}}", StaticUris.KATEX_CSS)
    .replace("{{KATEX-RENDER}}", StaticUris.KATEX_RENDER)
    .replace("{{PRISM}}", StaticUris.PRISM)
    .replace("{{PRISM-LIGHT-CSS}}", StaticUris.PRISM_LIGHT_CSS)
    .replace("{{PRISM-DARK-CSS}}", StaticUris.PRISM_DARK_CSS)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Suppress("SetJavaScriptEnabled")
@Composable
actual fun MarkdownViewer(
    modifier: Modifier,
    textFieldState: TextFieldState,
    firstVisibleCharPositon: Int,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>,
    styles: MarkdownStyles,
    appConfig: AppConfig
) {
    val scope = rememberCoroutineScope()
    val template = remember(styles, appConfig) {
        HTMLTemplate.processHtml(styles, appConfig)
    }

    var webView by remember { mutableStateOf<WebView?>(null) }

    // Effect to initialize JavaFX environment and WebView
    DisposableEffect(Unit) {
        Platform.runLater { // Run on JavaFX Application Thread
            val wv = WebView().also { webView = it }
            val engine = wv.engine.apply { isJavaScriptEnabled = true }

            // --- Link Click Handling ---
            engine.locationProperty().addListener { _, oldLocation, newLocation ->
                // Listen for changes in the WebView's location URL
                if (newLocation != null && newLocation != oldLocation && newLocation != "about:blank") {
                    // Check if it's an external link we should handle
                    if (newLocation.startsWith("http://") || newLocation.startsWith("https://")) {
                        try {
                            // Try to open the link in the default system browser
                            if (Desktop.isDesktopSupported()
                                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
                            ) {
                                Desktop.getDesktop().browse(URI(newLocation))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Platform.runLater {
                            // IMPORTANT: Prevent the WebView from navigating by reloading the original content
                            // The loadWorker listener above will handle restoring the scroll position.
                            engine.loadContent(template, "text/html")
                        }
                    }
                }
            }

            // --- Loading State Handling ---
            engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState === Worker.State.SUCCEEDED) {
                    scope.launch {
                        snapshotFlow { textFieldState.text }
                            .debounce(200L)
                            .mapLatest { markdownText ->
                                // Escape HTML content for safe injection into a JavaScript template literal
                                processMarkdown(markdownText.toString()).escaped()
                            }
                            .flowOn(Dispatchers.Default)
                            .collect { escapedHtml ->
                                val script = """
                                    if (typeof updateMarkdownContent === 'function') {
                                        updateMarkdownContent(`$escapedHtml`);
                                    } else {
                                        console.error('updateMarkdownContent function not found');
                                    }
                                """.trimIndent()
                                Platform.runLater {
                                    wv.engine.executeScript(script)
                                }
                            }
                    }
                } else if (newState === Worker.State.FAILED) {
                    println("WebView failed to load content.")
                }
            }

            jfxPanel.scene = Scene(wv)
            jfxPanel.enableMouseEvent()
        }

        onDispose {
            Platform.runLater {
                webView?.engine?.load(null)
                jfxPanel.scene = null // Clean up scene
            }
        }
    }

    // Embed the JFXPanel using SwingPanel
    SwingPanel(
        factory = { jfxPanel },
        modifier = modifier,
        background = MaterialTheme.colorScheme.background
    )

    LaunchedEffect(template, webView) {
        webView?.let {
            Platform.runLater {
                it.engine.loadContent(template)
            }
        }
    }

    LaunchedEffect(firstVisibleCharPositon, webView) {
        webView?.let {
            val script = firstVisibleCharPositon.createScrollToOffsetScript()
            Platform.runLater {
                if (it.engine.loadWorker.state == Worker.State.SUCCEEDED) {
                    it.engine.executeScript(script)
                }
            }
        }
    }

    val isUnlocked by AppLockManager.isUnlocked.collectAsStateWithLifecycle()
    LaunchedEffect(isSheetVisible, isUnlocked) {
        if (isSheetVisible || !isUnlocked) {
            jfxPanel.disableMouseEvent()
        } else {
            jfxPanel.enableMouseEvent()
        }
    }

    LaunchedEffect(printTrigger.value) {
        if (!printTrigger.value) return@LaunchedEffect
        webView?.let {
            Platform.runLater {
                try {
                    val printerJob = javafx.print.PrinterJob.createPrinterJob()
                    if (printerJob != null && printerJob.showPrintDialog(it.scene.window)) {
                        it.engine.print(printerJob)
                        printerJob.endJob()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    printTrigger.value = false
                }
            }
        }
    }
}

class InteropPanel : JFXPanel() {

    private var mouseEventEnabled = true

    init {
        Platform.setImplicitExit(false)
    }

    override fun processMouseEvent(e: MouseEvent) {
        if (mouseEventEnabled) super.processMouseEvent(e)
        else dispatchToCompose(e)
    }

    override fun processMouseWheelEvent(e: MouseWheelEvent) {
        if (mouseEventEnabled) super.processMouseWheelEvent(e)
        else dispatchToCompose(e)
    }

    override fun processMouseMotionEvent(e: MouseEvent) {
        if (mouseEventEnabled) super.processMouseMotionEvent(e)
        else dispatchToCompose(e)
    }

    fun enableMouseEvent() {
        mouseEventEnabled = true
    }

    fun disableMouseEvent() {
        mouseEventEnabled = false
    }

    private fun dispatchToCompose(e: MouseEvent) {
        when (e.id) {
            MouseEvent.MOUSE_ENTERED, MouseEvent.MOUSE_EXITED -> return
        }
        parent.dispatchEvent(e)
    }
}