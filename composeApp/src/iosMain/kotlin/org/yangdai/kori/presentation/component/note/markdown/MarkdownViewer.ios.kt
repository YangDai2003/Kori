package org.yangdai.kori.presentation.component.note.markdown

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
import androidx.compose.ui.viewinterop.UIKitView
import kori.composeapp.generated.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.Placeholders
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.createScrollToOffsetScript
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.escaped
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults.processMarkdown
import org.yangdai.kori.presentation.theme.AppConfig
import org.yangdai.kori.presentation.util.toHexColor
import org.yangdai.kori.presentation.util.toUIColor
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInfoOutputType
import platform.UIKit.UIPrintInteractionController
import platform.UIKit.viewPrintFormatter
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKURLSchemeHandlerProtocol
import platform.WebKit.WKURLSchemeTaskProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled
import platform.darwin.NSObject
import kotlin.math.roundToInt

internal const val IOS_CUSTOM_SCHEME = "note-files"

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

@OptIn(ExperimentalForeignApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
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
        HTMLTemplate.processHtml(styles, appConfig)
    }

    var webView by remember { mutableStateOf<WKWebView?>(null) }
    val navigationDelegate = remember { NavigationDelegate() }
    val schemeHandler = remember { LocalFileSchemeHandler() }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration().apply {
                preferences.javaScriptEnabled = true
                setURLSchemeHandler(schemeHandler, IOS_CUSTOM_SCHEME)
            }

            WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config).apply {
                this.opaque = false // To stop 'white flash'
                this.backgroundColor = styles.backgroundColor.toUIColor()
                this.navigationDelegate = navigationDelegate // Handle link clicks
                this.scrollView.showsVerticalScrollIndicator = true
                this.scrollView.showsHorizontalScrollIndicator = false
            }.also { webView = it }
        },
        modifier = modifier,
        onRelease = { wv ->
            wv.stopLoading()
            wv.navigationDelegate = null // Break reference cycle
            webView = null // Clear the state variable
        }
    )

    LaunchedEffect(template, webView) {
        val currentWebView = webView ?: return@LaunchedEffect
        currentWebView.backgroundColor = styles.backgroundColor.toUIColor()
        currentWebView.loadHTMLString(template, baseURL = NSBundle.mainBundle.resourceURL)
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
                currentWebView.evaluateJavaScript(script, null)
            }
    }

    LaunchedEffect(firstVisibleCharPositon, webView) {
        webView?.let {
            val script = firstVisibleCharPositon.createScrollToOffsetScript()
            it.evaluateJavaScript(script, null)
        }
    }

    LaunchedEffect(printTrigger.value) {
        if (!printTrigger.value) return@LaunchedEffect
        webView?.let {
            val printController = UIPrintInteractionController.sharedPrintController()
            val printInfo = UIPrintInfo.printInfo()
            printInfo.setOutputType(UIPrintInfoOutputType.UIPrintInfoOutputGeneral) // General output type, allows saving as PDF
            printInfo.setJobName("Markdown PDF")
            printController.setPrintInfo(printInfo)
            printController.setPrintFormatter(it.viewPrintFormatter())
            printController.presentAnimated(true, null)
        }
        printTrigger.value = false
    }
}


@OptIn(ExperimentalForeignApi::class)
private class LocalFileSchemeHandler : NSObject(), WKURLSchemeHandlerProtocol {

    private val fileManager = NSFileManager.defaultManager
    private val documentsDirectoryURL: NSURL? =
        fileManager.URLsForDirectory(
            platform.Foundation.NSDocumentDirectory,
            platform.Foundation.NSUserDomainMask
        ).firstOrNull() as? NSURL

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, startURLSchemeTask: WKURLSchemeTaskProtocol) {
        val request = startURLSchemeTask.request
        val url = request.URL ?: run {
            startURLSchemeTask.didFailWithError(
                NSError.errorWithDomain(
                    "LocalFileSchemeHandlerError",
                    code = 1,
                    userInfo = null
                )
            )
            return
        }

        // 从 URL 中提取文件名 (去除 scheme 和 "://")
        val pathComponent = url.resourceSpecifier // 通常是 scheme 后面的部分

        if (pathComponent == null || pathComponent.isBlank()) {
            startURLSchemeTask.didFailWithError(
                NSError.errorWithDomain(
                    "LocalFileSchemeHandlerError",
                    code = 2,
                    userInfo = null
                )
            )
            return
        }

        val targetFileUrl = documentsDirectoryURL?.URLByAppendingPathComponent(pathComponent)

        if (targetFileUrl == null || !fileManager.fileExistsAtPath(targetFileUrl.path!!)) {
            val error = NSError.errorWithDomain(
                "LocalFileSchemeHandlerError", code = 404, userInfo = mapOf<Any?, Any>(
                    platform.Foundation.NSLocalizedDescriptionKey to "File not found for scheme at path $pathComponent"
                )
            )
            startURLSchemeTask.didFailWithError(error)
            return
        }

        try {
            val fileData = NSData.dataWithContentsOfURL(targetFileUrl)
            if (fileData == null) {
                startURLSchemeTask.didFailWithError(
                    NSError.errorWithDomain(
                        "LocalFileSchemeHandlerError",
                        code = 3,
                        userInfo = null
                    )
                )
                return
            }

            // 推断 MIME 类型 (简化版)
            val mimeType = getMimeTypeForPathExtension(targetFileUrl.pathExtension())

            val response = NSHTTPURLResponse(
                uRL = url,
                statusCode = 200,
                HTTPVersion = "HTTP/1.1",
                headerFields = mapOf<Any?, Any>(
                    "Content-Type" to mimeType,
                    "Content-Length" to fileData.length.toString()
                )
            )

            startURLSchemeTask.didReceiveResponse(response)
            startURLSchemeTask.didReceiveData(fileData)
            startURLSchemeTask.didFinish()

        } catch (e: Exception) {
            // 在 Kotlin/Native 中，NSError 可能不是直接通过 throw/catch 捕获
            // 这里假设 e 是某种可以转换为 NSError 的异常
            val nsError = NSError.errorWithDomain(
                "LocalFileSchemeHandlerError",
                code = 4,
                userInfo = mapOf<Any?, Any>(
                    platform.Foundation.NSLocalizedDescriptionKey to (e.message ?: "Unknown error")
                )
            )
            startURLSchemeTask.didFailWithError(nsError)
        }
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, stopURLSchemeTask: WKURLSchemeTaskProtocol) {
        // 当请求被取消或 WebView 被销毁时调用。
        println("Scheme task stopped for: ${stopURLSchemeTask.request.URL?.absoluteString}")
    }

    private fun getMimeTypeForPathExtension(pathExtension: String?): String {
        return when (pathExtension?.lowercase()) {
            "jpeg", "jpg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            // 根据需要添加更多 MIME 类型
            else -> "application/octet-stream" // 默认二进制流
        }
    }
}

private class NavigationDelegate : NSObject(), WKNavigationDelegateProtocol {

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        val url = decidePolicyForNavigationAction.request.URL
        // The navigationType variable holds the enum value directly
        val navigationType = decidePolicyForNavigationAction.navigationType

        // Check if it's a link click
        // Corrected comparison: Use the imported constant directly
        if (url != null && navigationType == WKNavigationTypeLinkActivated) {
            // Intercept clicks on links
            val scheme = url.scheme?.lowercase()
            if (scheme == "http" || scheme == "https") {
                // Check if UIApplication can open the URL and open it externally
                val application = UIApplication.sharedApplication
                if (application.canOpenURL(url)) {
                    application.openURL(url, emptyMap<Any?, Any>(), null)
                    // Cancel the navigation within the WebView
                    decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel) // Use WKNavigationActionPolicyCancel constant
                    return
                }
            }
        }

        // Allow other navigation actions (initial load, redirects, etc.)
        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow) // Use WKNavigationActionPolicyAllow constant
    }
}