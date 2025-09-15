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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import org.yangdai.kori.presentation.component.dialog.ImageViewerDialog
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.Placeholders
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.createScrollToOffsetScript
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.escaped
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.processMarkdown
import org.yangdai.kori.presentation.theme.AppConfig
import org.yangdai.kori.presentation.util.toHexColor
import java.io.File
import kotlin.math.roundToInt

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@SuppressLint("SetJavaScriptEnabled")
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
    val template = remember(styles, appConfig) {
        HTMLTemplate
            .replace(Placeholders.TEXT_COLOR, styles.hexTextColor)
            .replace(Placeholders.BACKGROUND_COLOR, styles.backgroundColor.toHexColor())
            .replace(Placeholders.CODE_BACKGROUND, styles.hexCodeBackgroundColor)
            .replace(Placeholders.PRE_BACKGROUND, styles.hexPreBackgroundColor)
            .replace(Placeholders.QUOTE_BACKGROUND, styles.hexQuoteBackgroundColor)
            .replace(Placeholders.LINK_COLOR, styles.hexLinkColor)
            .replace(Placeholders.BORDER_COLOR, styles.hexBorderColor)
            .replace(Placeholders.COLOR_SCHEME, if (appConfig.darkMode) "dark" else "light")
            .replace(Placeholders.FONT_SCALE, "${(appConfig.fontScale * 100).roundToInt()}%")
    }

    var webView by remember { mutableStateOf<WebView?>(null) }
    var clickedImageUrl by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = {
            WebView(it).apply {
                setBackgroundColor(Color.Transparent.toArgb())
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                clipToOutline = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webView = this
                webViewClient = WVClient(it)
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
                settings.apply {
                    allowFileAccess = true
                    allowContentAccess = true
                    domStorageEnabled = true
                    javaScriptEnabled = true
                    loadsImagesAutomatically = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = false
                }
                addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun onImageClick(urlStr: String) {
                            clickedImageUrl = urlStr
                        }
                    },
                    "imageInterface"
                )
            }
        },
        onReset = {
            it.stopLoading()
            it.loadUrl("about:blank")
            it.clearHistory()
            webView = null
        }
    )

    LaunchedEffect(webView, template) {
        val currentWebView = webView ?: return@LaunchedEffect
        currentWebView.loadDataWithBaseURL(
            "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/",
            template,
            "text/html",
            "UTF-8",
            null
        )
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
                currentWebView.evaluateJavascript(script, null)
            }
    }

    LaunchedEffect(firstVisibleCharPositon, webView) {
        webView?.let {
            val script = firstVisibleCharPositon.createScrollToOffsetScript()
            it.evaluateJavascript(script, null)
        }
    }

    val activity = LocalActivity.current
    LaunchedEffect(printTrigger.value) {
        if (!printTrigger.value) return@LaunchedEffect
        webView?.let { createWebPrintJob(it, activity) }
        printTrigger.value = false
    }

    clickedImageUrl?.let {
        ImageViewerDialog(
            onDismissRequest = { clickedImageUrl = null },
            imageUrl = it,
        )
    }
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