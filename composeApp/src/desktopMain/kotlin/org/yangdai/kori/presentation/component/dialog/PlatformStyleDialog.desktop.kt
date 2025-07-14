package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogWindow
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ConfirmButton(text: String, onClick: () -> Unit) {
}

@Composable
actual fun DismissButton(text: String, onClick: () -> Unit) {
}

@Composable
actual fun PlatformStyleDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?
) = DialogWindow(
    onCloseRequest = onDismissRequest,
    title = stringResource(Res.string.app_name)
) {
    Column {
        title?.invoke()
        text?.invoke()
        Row {
            dismissButton?.invoke()
            confirmButton()
        }
    }
}