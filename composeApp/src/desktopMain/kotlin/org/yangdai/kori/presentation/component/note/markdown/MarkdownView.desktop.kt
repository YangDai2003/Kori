package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.text.TextRange
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.awt.Desktop
import java.net.URI

@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    selection: TextRange,
    scrollState: ScrollState,
    isAppInDarkTheme: Boolean,
    styles: MarkdownStyles
) {
    // State to hold the WebView for access in LaunchedEffect and update
    var webView by remember { mutableStateOf<WebView?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            webView = null
        }
    }

    val data by remember(html, styles, isAppInDarkTheme) {
        mutableStateOf(
            processHtml(html, styles, isAppInDarkTheme)
        )
    }

    SwingPanel(
        factory = {
            JFXPanel().also { jfxPanel ->
                Platform.runLater {
                    val wv = WebView()
                    webView = wv

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
                    }

                    val scene = Scene(wv)
                    jfxPanel.scene = scene
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
