package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    selection: TextRange,
    scrollState: ScrollState,
    isAppInDarkTheme: Boolean,
    styles: MarkdownStyles
) {
    // State to hold the WKWebView instance for access in LaunchedEffect
    var webView by remember { mutableStateOf<WKWebView?>(null) }
    // Remember the navigation delegate instance
    val navigationDelegate = remember { NavigationDelegate() }

    val data by remember(html, styles, isAppInDarkTheme) {
        mutableStateOf(
            processHtml(html, styles, isAppInDarkTheme)
        )
    }

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
        webViewInstance.evaluateJavaScript(script, null)
    }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration()
            config.preferences().javaScriptEnabled = true

            WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config).apply {
                this.opaque = false // To stop 'white flash'
                this.scrollView.backgroundColor =
                    UIColor.clearColor // Ensure scroll view is also transparent

                // --- Configuration similar to Android ---
                this.navigationDelegate = navigationDelegate // Handle link clicks

                this.scrollView.showsVerticalScrollIndicator = true
                this.scrollView.showsHorizontalScrollIndicator = false
                this.scrollView.bounces = false // Disable bounce effect
            }.also { webView = it }
        },
        modifier = modifier, // Apply Compose modifiers
        update = { wv -> wv.loadHTMLString(data, baseURL = null) },
        onRelease = { wv ->
            wv.stopLoading()
            wv.navigationDelegate = null // Break reference cycle
            webView = null // Clear the state variable
        }
    )
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
                    application.openURL(
                        url,
                        options = emptyMap<Any?, Any>(),
                        completionHandler = null
                    )
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

