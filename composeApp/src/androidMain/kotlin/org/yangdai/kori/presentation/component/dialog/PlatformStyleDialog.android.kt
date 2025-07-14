package org.yangdai.kori.presentation.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)?
) = AlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    title = title,
    text = text,
    confirmButton = confirmButton,
    dismissButton = dismissButton
)