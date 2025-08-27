package org.yangdai.kori.presentation.component.dialog

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.confirm
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ConfirmButton(colors: ButtonColors, onClick: () -> Unit) =
    Button(onClick, colors = colors) { Text(stringResource(Res.string.confirm)) }

@Composable
actual fun DismissButton(onClick: () -> Unit) =
    TextButton(onClick) { Text(stringResource(Res.string.cancel)) }

@Composable
actual fun dialogShape(): Shape = AlertDialogDefaults.shape