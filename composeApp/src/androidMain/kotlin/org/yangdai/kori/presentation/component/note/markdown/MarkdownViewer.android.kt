package org.yangdai.kori.presentation.component.note.markdown

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yangdai.kori.presentation.component.dialog.ImageViewerDialog
import org.yangdai.kori.presentation.theme.LocalAppConfig
import org.yangdai.kori.presentation.util.toHexColor
import java.io.File
import java.io.InputStreamReader
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun MarkdownViewer(
    modifier: Modifier,
    html: String,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>,
    styles: MarkdownStyles
) {
    val activity = LocalActivity.current
    val appConfig = LocalAppConfig.current
    val assets = LocalResources.current.assets

    var webView by remember { mutableStateOf<WebView?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var clickedImageUrl by remember { mutableStateOf("") }
    var htmlTemplate by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            htmlTemplate = runCatching {
                InputStreamReader(assets.open("template_for_android.html")).use { it.readText() }
            }.getOrDefault("")
        }
    }
    val data = remember(html, styles, appConfig, htmlTemplate) {
        if (htmlTemplate.isEmpty()) ""
        else htmlTemplate
            .replace("{{TEXT_COLOR}}", styles.hexTextColor)
            .replace("{{BACKGROUND_COLOR}}", styles.backgroundColor.toHexColor())
            .replace("{{CODE_BACKGROUND}}", styles.hexCodeBackgroundColor)
            .replace("{{PRE_BACKGROUND}}", styles.hexPreBackgroundColor)
            .replace("{{QUOTE_BACKGROUND}}", styles.hexQuoteBackgroundColor)
            .replace("{{LINK_COLOR}}", styles.hexLinkColor)
            .replace("{{BORDER_COLOR}}", styles.hexBorderColor)
            .replace("{{COLOR_SCHEME}}", if (appConfig.darkMode) "dark" else "light")
            .replace("{{FONT_SCALE}}", "${(appConfig.fontScale * 100).roundToInt()}%")
            .replace("{{CONTENT}}", html)
    }

    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = {
            WebView(it).apply {
                webView = this
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun onImageClick(urlStr: String) {
                            clickedImageUrl = urlStr
                            showDialog = true
                        }
                    },
                    "imageInterface"
                )
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
                settings.apply {
                    domStorageEnabled = true
                    javaScriptEnabled = true
                    loadsImagesAutomatically = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = false
                }
                this.webViewClient = WVClient(it)
                setBackgroundColor(Color.Transparent.toArgb())
            }
        },
        onReset = {
            it.clearHistory()
            it.stopLoading()
            it.destroy()
            webView = null
        }
    )

    LaunchedEffect(data, webView) {
        webView?.loadDataWithBaseURL(
            "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/",
            data,
            "text/html",
            "UTF-8",
            null
        )
    }

    LaunchedEffect(scrollState.value, scrollState.maxValue) {
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

        webViewInstance.evaluateJavascript(script, null)
    }

    LaunchedEffect(printTrigger.value) {
        if (!printTrigger.value) return@LaunchedEffect
        webView?.let { createWebPrintJob(it, activity) }
        printTrigger.value = false
    }

    if (showDialog)
        ImageViewerDialog(
            onDismissRequest = { showDialog = false },
            imageUrl = clickedImageUrl,
        )
}

private class WVClient(context: Context) : WebViewClient() {
    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .addPathHandler(
            "/files/",
            WebViewAssetLoader.InternalStoragePathHandler(context, File(context.filesDir, ""))
        )
        .build()

    private val customTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        val url = request.url.toString()
        if (!url.startsWith("https://${WebViewAssetLoader.DEFAULT_DOMAIN}")) {
            customTabsIntent.launchUrl(view.context, request.url)
        }
        return true
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }
}

private fun createWebPrintJob(webView: WebView, activity: Activity?) {
    (activity?.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
        val printAdapter = webView.createPrintDocumentAdapter("markdown_export")
        val builder = PrintAttributes.Builder()
            // 一般为 0.5-1 英寸（12.7-25.4 毫米）的边距
            .setMinMargins(PrintAttributes.Margins(36, 36, 36, 36))
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        printManager.print("Markdown PDF", printAdapter, builder.build())
    }
}