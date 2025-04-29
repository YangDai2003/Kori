package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.WebKit.*
import platform.darwin.NSObject
import platform.CoreGraphics.CGRectMake

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MarkdownView(
    modifier: Modifier,
    html: String,
    scrollState: ScrollState, // Used for scrolling the WebView via JS
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

    // Effect to synchronize Compose ScrollState -> WKWebView scroll position
    LaunchedEffect(webView, scrollState.value) {
        val currentWebView = webView ?: return@LaunchedEffect // Exit if webView is null

        val totalHeight = scrollState.maxValue // Max scroll position from Compose state
        val currentScrollPercent = when {
            // Calculate scroll percentage from Compose ScrollState
            totalHeight <= 0 -> 0f
            scrollState.value >= totalHeight -> 1f
            else -> (scrollState.value.toFloat() / totalHeight).coerceIn(0f, 1f)
        }

        // JavaScript to scroll the web page based on the percentage
        // Note: document.body.scrollHeight might be more reliable across content types
        val jsCode = """
        (function() {
            const scrollableHeight = document.body.scrollHeight - window.innerHeight;
            if (scrollableHeight > 0) {
                 window.scrollTo({
                    top: scrollableHeight * $currentScrollPercent,
                    behavior: 'auto' // Use 'auto' for immediate jump matching state
                 });
            } else {
                 // Content is not scrollable or height calculation failed, maybe scroll to top
                 window.scrollTo({ top: 0, behavior: 'auto' });
            }
        })();
        """.trimIndent()

        currentWebView.evaluateJavaScript(jsCode, null)
    }

    UIKitView(
        factory = {
            // Create configuration
            val config = WKWebViewConfiguration()
            // Optionally disable JavaScript if not needed for content itself (though needed for scroll sync)
            // config.preferences.javaScriptEnabled = true // Default is true

            // Create WebView
            val wv =
                WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config).apply {
                    this.opaque = false // Make WebView background transparent
                    // Set background color explicitly (or rely solely on CSS body background)
                    // this.backgroundColor = UIColor.clearColor
                    this.scrollView.backgroundColor =
                        UIColor.clearColor // Ensure scroll view is also transparent

                    // --- Configuration similar to Android ---
                    this.navigationDelegate = navigationDelegate // Handle link clicks

                    this.scrollView.showsVerticalScrollIndicator = false
                    this.scrollView.showsHorizontalScrollIndicator = false
                    this.scrollView.bounces = false // Disable bounce effect

                    // Disable zoom - handled by viewport meta tag + potentially disabling gestures
                    // The viewport tag in generateHtml ("user-scalable=no") is the primary method.
                    // For extra measure, disabling pinch gesture (might interfere with selection):
                    // this.scrollView.pinchGestureRecognizer?.enabled = false
                }
            webView = wv // Store the created webview instance
            wv // Return the configured WebView
        },
        modifier = modifier, // Apply Compose modifiers
        update = { wv ->
            // Check if content actually changed to avoid unnecessary reloads
            // NOTE: This simple check might not be sufficient if only styles/theme changed
            // The `remember` block handles this better. Just load the latest htmlContent.
            wv.loadHTMLString(data, baseURL = null)
        },
        onRelease = { wv ->
            // Cleanup when the Composable leaves the composition
            wv.stopLoading()
            wv.navigationDelegate = null // Break reference cycle
            webView = null // Clear the state variable
            // WKWebView doesn't have an explicit destroy() like Android.
            // Setting delegate to null and removing from view hierarchy handles cleanup.
        }
    )
}

// --- Navigation Delegate ---
// Needs to inherit NSObject to be used as a delegate
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
                    application.openURL(url)
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

