package org.yangdai.kori.presentation.component.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun LoginDialog(content: @Composable (() -> Unit)) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            usePlatformInsets = false,
            useSoftwareKeyboardInset = false,
            scrimColor = Color.Transparent
        ),
        content = content
    )
}