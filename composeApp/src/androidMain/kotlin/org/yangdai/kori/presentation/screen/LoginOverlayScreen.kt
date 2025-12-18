package org.yangdai.kori.presentation.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationRequest.Companion.biometricRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import org.yangdai.kori.R
import org.yangdai.kori.presentation.component.login.NumberLockScreen

@Composable
fun LoginOverlayScreen(
    storedPassword: String,
    isCreatingPassword: Boolean,
    biometricAuthEnabled: Boolean,
    onCreatingCanceled: () -> Unit,
    onPassCreated: (String) -> Unit,
    onAuthenticated: () -> Unit,
    onAuthenticationNotEnrolled: () -> Unit
) {
    val context = LocalContext.current
    val enrollBiometricsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}
    val biometricLauncher = rememberAuthenticationLauncher { result ->
        when (result) {
            is AuthenticationResult.Error -> {
                Toast.makeText(
                    context,
                    "${"Err:${result.errorCode} "}${result.errString}",
                    Toast.LENGTH_SHORT
                ).show()

                if (result.errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                    onAuthenticationNotEnrolled()
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_WEAK
                            )
                        }
                    } else null)?.let { enrollBiometricsLauncher.launch(it) }
                }
            }

            is AuthenticationResult.Success -> onAuthenticated()
        }
    }

    val title = stringResource(R.string.unlock_to_use)
    val negativeButtonText = org.jetbrains.compose.resources.stringResource(Res.string.cancel)
    val modifier: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f))
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    }

    NumberLockScreen(
        modifier = modifier,
        storedPassword = storedPassword,
        isBiometricAuthEnabled = biometricAuthEnabled,
        isCreatingPassword = isCreatingPassword,
        onCreatingCanceled = onCreatingCanceled,
        onPassCreated = onPassCreated,
        onBiometricClick = {
            biometricLauncher.launch(
                biometricRequest(
                    title = title,
                    authFallback = AuthenticationRequest.Biometric.Fallback.NegativeButton(
                        negativeButtonText = negativeButtonText
                    )
                ) {
                    setIsConfirmationRequired(false)
                }
            )
        },
        onAuthenticated = onAuthenticated
    )
}
