package org.yangdai.kori.presentation.component.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NumberLockScreen(
    modifier: Modifier = Modifier,
    storedPassword: String = "1234",
    isBiometricAuthEnabled: Boolean = false,
    isCreatingPassword: Boolean = false,
    onCreatingCanceled: () -> Unit = {},
    onPassCreated: (String) -> Unit = {},
    onFingerprintClick: () -> Unit = {},
    onAuthenticated: () -> Unit = {}
) {
    // Your implementation here
}