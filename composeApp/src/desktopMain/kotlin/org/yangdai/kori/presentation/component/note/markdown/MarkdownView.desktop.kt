package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.awt.Desktop
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.net.URI

@Suppress("SetJavaScriptEnabled")
@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    selection: TextRange,
    scrollState: ScrollState,
    isAppInDarkTheme: Boolean,
    styles: MarkdownStyles,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>
) {
    val jfxPanel = remember { InteropPanel() }
    var webView by remember { mutableStateOf<WebView?>(null) }
    // Store the latest processed HTML data for link interception reload
    var latestData by remember { mutableStateOf("") }
    // Store the scroll position before loading new content
    var scrollPositionBeforeLoad by remember { mutableDoubleStateOf(0.0) } // Use Double for JS number

    val data by remember(html, styles, isAppInDarkTheme) {
        derivedStateOf {
            processHtml(html, styles, isAppInDarkTheme)
        }
    }

    // Effect to initialize JavaFX environment and WebView
    DisposableEffect(Unit) {
        Platform.setImplicitExit(false)
        Platform.runLater { // Run on JavaFX Application Thread
            val wv = WebView()
            val engine = wv.engine
            engine.isJavaScriptEnabled = true

            // --- Listener for Load Completion ---
            // This listener will restore the scroll position after any load finishes
            engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState == Worker.State.SUCCEEDED) {
                    // Restore scroll position AFTER load is complete
                    val scrollY = scrollPositionBeforeLoad
                    Platform.runLater { // Ensure execution on FX thread
                        // Use 'auto' behavior for instant jump, not smooth scroll
                        engine.executeScript("window.scrollTo({ top: $scrollY, behavior: 'auto' });")
                    }
                }
                // Optionally handle FAILED state
                else if (newState == Worker.State.FAILED) {
                    // Handle error, maybe log engine.loadWorker.exception
                    println("WebView load failed: ${engine.loadWorker.exception}")
                }
            }


            // --- Link Click Handling ---
            engine.locationProperty().addListener { _, oldLocation, newLocation ->
                // Listen for changes in the WebView's location URL
                if (newLocation != null && newLocation != oldLocation && newLocation != "about:blank") {
                    // Check if it's an external link we should handle
                    if (newLocation.startsWith("http://") || newLocation.startsWith("https://")) {

                        // --- Capture scroll position BEFORE reloading for link interception ---
                        Platform.runLater { // Get scroll on FX thread
                            val scrollYObject =
                                engine.executeScript("window.scrollY || document.documentElement.scrollTop || document.body.scrollTop || 0;")
                            // Convert the result carefully (it might be Int, Double, etc.)
                            scrollPositionBeforeLoad = (scrollYObject as? Number)?.toDouble() ?: 0.0

                            try {
                                // Try to open the link in the default system browser
                                if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                        .isSupported(Desktop.Action.BROWSE)
                                ) {
                                    Desktop.getDesktop().browse(URI(newLocation))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            // IMPORTANT: Prevent the WebView from navigating by reloading the original content
                            // The loadWorker listener above will handle restoring the scroll position.
                            engine.loadContent(latestData, "text/html")
                        }
                    }
                }
            }

            webView = wv
            jfxPanel.scene = Scene(wv)
            jfxPanel.enableMouseEvent()
        }

        onDispose {
            Platform.runLater {
                webView?.engine?.load(null) // Stop loading
                webView?.engine?.loadWorker?.cancel() // Cancel any ongoing work
                webView = null
                jfxPanel.scene = null // Clean up scene
            }
        }
    }

    // Embed the JFXPanel using SwingPanel
    SwingPanel(
        factory = { jfxPanel },
        modifier = modifier,
        background = Color(styles.backgroundColor),
        update = {
            Platform.runLater { // Get scroll on FX thread
                if (webView == null) return@runLater
                val scrollYObject =
                    webView!!.engine.executeScript("window.scrollY || document.documentElement.scrollTop || document.body.scrollTop || 0;")
                // Convert the result carefully (it might be Int, Double, etc.)
                scrollPositionBeforeLoad = (scrollYObject as? Number)?.toDouble() ?: 0.0

                // Now trigger the load. The loadWorker listener will handle restoring scroll.
                latestData = data // Update data used for link interception too
                webView!!.engine.loadContent(latestData, "text/html")
            }
        }
    )

    LaunchedEffect(scrollState.value, scrollState.maxValue, webView) {
        val webViewInstance = webView ?: return@LaunchedEffect
        val totalHeight = scrollState.maxValue
        val currentScroll = scrollState.value
        if (totalHeight <= 0) return@LaunchedEffect

        // Calculate scroll percentage (0.0 to 1.0)
        val currentScrollPercent = (currentScroll.toFloat() / totalHeight).coerceIn(0f, 1f)
        val script = """
        (function() {
            // Only scroll if not currently loading to avoid conflicts
             if (document.readyState === 'complete' || document.readyState === 'interactive') { // Basic check
                const d = document.documentElement;
                const b = document.body;
                const maxHeight = Math.max(
                    d.scrollHeight, d.offsetHeight, d.clientHeight,
                    b.scrollHeight, b.offsetHeight
                );
                window.scrollTo({
                    top: maxHeight * $currentScrollPercent,
                    behavior: 'auto' // Use 'auto' for immediate jump syncing with ScrollState
                });
             }
        })();
        """.trimIndent()

        Platform.runLater {
            // Check worker state again for safety before executing scroll based on external state
            if (webViewInstance.engine.loadWorker.state == Worker.State.SUCCEEDED) {
                webViewInstance.engine.executeScript(script)
            }
        }
    }

    LaunchedEffect(isSheetVisible) {
        if (isSheetVisible) {
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
                    if (printerJob != null && printerJob.showPrintDialog(null)) {
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

private class InteropPanel : JFXPanel() {

    private var mouseEventEnabled = true

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
