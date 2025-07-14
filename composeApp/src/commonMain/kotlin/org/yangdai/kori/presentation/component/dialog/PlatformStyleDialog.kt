package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ConfirmButton(
    text: String,
    onClick: () -> Unit
)

@Composable
expect fun DismissButton(
    text: String,
    onClick: () -> Unit
)

@Composable
expect fun PlatformStyleDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable (() -> Unit)? = null
)