package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import kink.InkScreen
import org.yangdai.kori.presentation.theme.KoriTheme
import platform.UIKit.UIApplication
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIWindow
import platform.UIKit.sheetPresentationController

@Composable
actual fun WidgetListItem() {
}

@Composable
actual fun InkListItem() =
    ListItem(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(CardDefaults.shape)
            .clickable {
                // 获取根视图控制器
                val window = UIApplication.sharedApplication.windows.first() as? UIWindow
                val rootViewController = window?.rootViewController

                // 创建承载 Bottom Sheet 内容的 UIViewController
                val viewController = ComposeUIViewController { KoriTheme { InkScreen() } }
                viewController.sheetPresentationController?.let { sheet ->
                    // 设置停留点
                    sheet.detents = listOf(UISheetPresentationControllerDetent.largeDetent())
                    // 显示顶部的拖拽指示器
                    sheet.prefersGrabberVisible = true
                }

                rootViewController?.presentViewController(
                    viewControllerToPresent = viewController,
                    animated = true,
                    completion = null
                )
            },
        headlineContent = { Text("Ink Playground") },
        supportingContent = { Text("This is an experimental feature and may be changed, presented in a different form, or even removed entirely.") },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = null
            )
        }
    )