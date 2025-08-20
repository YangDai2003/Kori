package org.yangdai.kori.presentation.component.note.markdown

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
import androidx.compose.ui.viewinterop.UIKitView
import kori.composeapp.generated.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.yangdai.kori.presentation.theme.LocalAppConfig
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

internal const val IOS_CUSTOM_SCHEME = "note-files"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>,
    styles: MarkdownStyles
) {
    var webView by remember { mutableStateOf<WKWebView?>(null) }
    val navigationDelegate = remember { NavigationDelegate() }
    val schemeHandler = remember { LocalFileSchemeHandler() }
    val appConfig = LocalAppConfig.current
    var htmlTemplate by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            htmlTemplate = runCatching {
                Res.readBytes("files/template.html").decodeToString()
            }.getOrDefault("")
        }
    }
    val data = remember(html, styles, appConfig, htmlTemplate) {
        processHtml(htmlTemplate, html, styles, appConfig)
    }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration()
            config.preferences().javaScriptEnabled = true
            config.setURLSchemeHandler(schemeHandler, IOS_CUSTOM_SCHEME)

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

    LaunchedEffect(data, webView) {
        webView?.loadHTMLString(data, baseURL = NSBundle.mainBundle.resourceURL)
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
        webViewInstance.evaluateJavaScript(script, null)
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
            printController.presentAnimated(
                true,
                completionHandler = { _, completed, error: NSError? ->
                    // Reset the trigger regardless of the outcome
                    printTrigger.value = false
                })
        }
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