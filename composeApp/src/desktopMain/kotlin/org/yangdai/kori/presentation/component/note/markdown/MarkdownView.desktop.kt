package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.web.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

// TODO 使用其他方法实现，FXJAVA 过于笨重
@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    scrollState: ScrollState,
    isAppInDarkTheme: Boolean,
    styles: MarkdownStyles
) {
    // State to hold the WebView for access in LaunchedEffect and update
    var webView by remember { mutableStateOf<WebView?>(null) }

    val data by remember(html, styles, isAppInDarkTheme) {
        mutableStateOf(
            processHtml(html, styles, isAppInDarkTheme)
        )
    }

    // Effect to sync scroll state (Compose -> WebView)
    LaunchedEffect(webView, scrollState.value) {
        val currentWebView = webView ?: return@LaunchedEffect
        val totalHeight = scrollState.maxValue
        val currentScrollPercent = when {
            totalHeight <= 0 -> 0f
            scrollState.value >= totalHeight -> 1f
            else -> (scrollState.value.toFloat() / totalHeight).coerceIn(0f, 1f)
        }

        // Execute JS on the JavaFX Application Thread
        withContext(Dispatchers.Swing) { // Switch to JavaFX thread
            try {
                currentWebView.engine.executeScript(
                    """
                    (function() {
                        const scrollableHeight = document.body.scrollHeight - window.innerHeight;
                        if (scrollableHeight > 0) {
                            window.scrollTo({
                                top: scrollableHeight * $currentScrollPercent,
                                behavior: 'auto'
                            });
                        } else {
                            window.scrollTo({ top: 0, behavior: 'auto' });
                        }
                    })();
                    """.trimIndent()
                )
            } catch (e: Exception) {
                // Handle potential JS errors (e.g., netscape.javascript.JSException)
                println("Error executing scroll script: ${e.message}")
            }
        }
    }

    SwingPanel(
        factory = {
            JFXPanel().also { jfxPanel ->
                Platform.runLater {
                    val wv = WebView()
                    webView = wv

                    // *** FIX: Make WebView fill its parent ***
                    wv.maxWidth = Double.MAX_VALUE
                    wv.maxHeight = Double.MAX_VALUE

                    wv.engine.apply {
                        loadWorker.stateProperty().addListener { _, _, newState ->
                            if (newState == Worker.State.SCHEDULED) {
                                val url = wv.engine.location
                                if (url != null && (url.startsWith("http://") || url.startsWith("https://")) && !wv.engine.history.entries.isEmpty()) {
                                    try {
                                        println("Intercepted link click: $url")
                                        wv.engine.loadWorker.cancel()
                                        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                                .isSupported(Desktop.Action.BROWSE)
                                        ) {
                                            Desktop.getDesktop().browse(URI(url))
                                        } else {
                                            println("Desktop Browse not supported.")
                                        }
                                    } catch (e: Exception) {
                                        println("Error opening link $url: ${e.message}")
                                    }
                                }
                            } else if (newState == Worker.State.FAILED) {
                                println("WebView load failed: ${wv.engine.loadWorker.exception}")
                            }
                        }
                        wv.isContextMenuEnabled = false
                    }

                    wv.engine.loadContent(data, "text/html")

                    // *** FIX: Use a layout pane (StackPane) to contain the WebView ***
                    // StackPane by default resizes its children to fill
                    val rootPane = StackPane(wv)
                    rootPane.maxWidth = Double.MAX_VALUE // Ensure the pane itself resizes
                    rootPane.maxHeight = Double.MAX_VALUE

                    // Set the root pane as the root of the scene
                    val scene = Scene(rootPane) // Use rootPane here
                    jfxPanel.scene = scene

                    // The JFXPanel should also resize to fill its Swing parent,
                    // which SwingPanel typically handles, but ensuring its size
                    // and layout managers are set up correctly can help.
                    // For simple cases like this within SwingPanel, setting the
                    // JavaFX root to fill the scene is usually sufficient.
                }
            }
        },
        modifier = modifier,
        background = androidx.compose.ui.graphics.Color.Transparent,
        update = { panel ->
            Platform.runLater {
                webView?.engine?.loadContent(data, "text/html")
            }
        }
    )
}
