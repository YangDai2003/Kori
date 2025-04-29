package org.yangdai.kori.presentation.component.note.markdown

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.enableSlowWholeDocumentDraw
import android.webkit.WebViewClient
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    scrollState: ScrollState,
    isAppInDarkTheme: Boolean,
    styles: MarkdownStyles
) {
    val uriHandler = LocalUriHandler.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    val data by remember(html, styles, isAppInDarkTheme) {
        mutableStateOf(
            processHtml(html, styles, isAppInDarkTheme)
        )
    }

    LaunchedEffect(scrollState.value) {
        val totalHeight = scrollState.maxValue
        val currentScrollPercent = when {
            totalHeight <= 0 -> 0f
            scrollState.value >= totalHeight -> 1f
            else -> (scrollState.value.toFloat() / totalHeight).coerceIn(0f, 1f)
        }

        webView?.evaluateJavascript(
            """
        (function() {
            const d = document.documentElement;
            const b = document.body;
            const maxHeight = Math.max(
                d.scrollHeight, d.offsetHeight, d.clientHeight,
                b.scrollHeight, b.offsetHeight
            );
            window.scrollTo({ 
                top: maxHeight * $currentScrollPercent, 
                behavior: 'auto' 
            });
        })();
        """.trimIndent(),
            null
        )
    }

    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = {
            WebView(it).also { webView = it }.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            uriHandler.openUri(url)
                        }
                        return true
                    }
                }
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                settings.domStorageEnabled = true
                settings.javaScriptEnabled = true
                settings.loadsImagesAutomatically = true
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.setSupportZoom(false)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = false
                enableSlowWholeDocumentDraw()
            }
        },
        update = {
            it.setBackgroundColor(styles.backgroundColor)
            it.loadDataWithBaseURL(
                null,
                data,
                "text/html",
                "UTF-8",
                null
            )
        },
        onReset = {
            it.clearHistory()
            it.stopLoading()
            it.destroy()
            webView = null
        })
}