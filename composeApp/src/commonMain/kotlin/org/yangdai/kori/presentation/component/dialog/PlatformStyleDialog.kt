package org.yangdai.kori.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

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
expect fun dialogShape(): Shape