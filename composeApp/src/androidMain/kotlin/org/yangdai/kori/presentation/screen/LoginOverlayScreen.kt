package org.yangdai.kori.presentation.screen

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import org.yangdai.kori.R
import org.yangdai.kori.presentation.component.login.NumberLockScreen
import org.yangdai.kori.presentation.util.BiometricPromptManager

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
    val activity = LocalActivity.current as AppCompatActivity
    val promptManager = remember(activity) { BiometricPromptManager(activity) }
    val biometricPromptResult by promptManager.promptResult.collectAsStateWithLifecycle(initialValue = null)

    val enrollBiometricsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    LaunchedEffect(biometricPromptResult) {
        when (biometricPromptResult) {
            is BiometricPromptManager.BiometricPromptResult.AuthenticationError -> {
                val error =
                    (biometricPromptResult as BiometricPromptManager.BiometricPromptResult.AuthenticationError).errString
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }

            BiometricPromptManager.BiometricPromptResult.AuthenticationFailed -> {
                Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }

            BiometricPromptManager.BiometricPromptResult.AuthenticationSucceeded -> {
                onAuthenticated()
            }

            BiometricPromptManager.BiometricPromptResult.AuthenticationNotEnrolled -> {
                // 处理未注册生物识别的情况
                promptManager.createEnrollBiometricsIntent()?.let {
                    enrollBiometricsLauncher.launch(it)
                } ?: onAuthenticationNotEnrolled()
            }

            null -> { /* 初始状态，不处理 */
            }
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
        onBiometricClick = { promptManager.showBiometricPrompt(title, negativeButtonText) },
        onAuthenticated = onAuthenticated
    )
}
