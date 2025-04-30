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

    // Effect to synchronize Editor Cursor -> WebView Scroll
    LaunchedEffect(selection) { // Trigger when cursorPosition changes
        val webViewInstance = webView ?: return@LaunchedEffect // Ensure webview is initialized
        val start = selection.min // For IntRange, min is inclusive
        val end = selection.max // For IntRange, last is inclusive
        // Ignore default/initial cursor position if it's often 0 or -1
        if (start < 0 || end < 0) {
            // Log.d("MarkdownScrollSync", "Ignoring invalid cursor position: $cursorPosition")
            return@LaunchedEffect
        }
        webViewInstance.evaluateJavaScript("scrollToRangePosition($start, $end);", null)
    }

    LaunchedEffect(scrollState.value) {
        val totalHeight = scrollState.maxValue
        val currentScrollPercent = when {
            totalHeight <= 0 -> 0f
            scrollState.value >= totalHeight -> 1f
            else -> (scrollState.value.toFloat() / totalHeight).coerceIn(0f, 1f)
        }

        webView?.evaluateJavaScript(
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

