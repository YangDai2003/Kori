package org.yangdai.kori.presentation.component.dialog

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

@Composable
expect fun ConfirmButton(colors: ButtonColors = ButtonDefaults.buttonColors(), onClick: () -> Unit)

@Composable
expect fun DismissButton(onClick: () -> Unit)

@Composable
expect fun dialogShape(): Shape