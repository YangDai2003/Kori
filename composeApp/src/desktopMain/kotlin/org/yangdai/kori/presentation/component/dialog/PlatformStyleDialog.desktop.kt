package org.yangdai.kori.presentation.component.dialog

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.confirm
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ConfirmButton(colors: ButtonColors, onClick: () -> Unit) =
    Button(
        onClick = onClick,
        colors = colors,
        shape = MaterialTheme.shapes.small
    ) {
        Text(stringResource(Res.string.confirm))
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun DismissButton(onClick: () -> Unit) =
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small
    ) {
        Text(stringResource(Res.string.cancel))
    }

@Composable
actual fun dialogShape(): Shape = MaterialTheme.shapes.medium