package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.warning
import org.jetbrains.compose.resources.stringResource

@Composable
@Preview
private fun WarningDialogPreview() {
    WarningDialog(
        message = "This is a warning message.",
        onDismissRequest = {},
        onConfirm = {}
    )
}

@Composable
fun WarningDialog(
    message: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) = AlertDialog(
    modifier = Modifier.widthIn(max = DialogMaxWidth),
    title = { Text(stringResource(Res.string.warning)) },
    text = { Text(message) },
    shape = dialogShape(),
    onDismissRequest = onDismissRequest,
    confirmButton = {
        val haptic = LocalHapticFeedback.current
        ConfirmButton(
            ButtonDefaults.buttonColors().copy(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onConfirm()
            onDismissRequest()
        }
    },
    dismissButton = { DismissButton(onDismissRequest) }
)
