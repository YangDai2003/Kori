package org.yangdai.kori.presentation.component.setting.detail

import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.keep_screen_on
import kori.composeapp.generated.resources.password
import kori.composeapp.generated.resources.password_description
import org.yangdai.kori.R
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
actual fun SecurityPane(mainViewModel: MainViewModel) {

    val context = LocalContext.current
    val securityPaneState by mainViewModel.securityPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = org.jetbrains.compose.resources.stringResource(Res.string.keep_screen_on),
            icon = Icons.Outlined.PhoneAndroid,
            trailingContent = {
                Switch(
                    checked = securityPaneState.keepScreenOn,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.KEEP_SCREEN_ON,
                            checked
                        )
                    }
                )
            }
        )

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(R.string.screen_protection),
            description = stringResource(R.string.screen_protection_detail),
            icon = if (securityPaneState.isScreenProtected) Icons.Outlined.VisibilityOff
            else Icons.Outlined.Visibility,
            trailingContent = {
                Switch(
                    checked = securityPaneState.isScreenProtected,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.IS_SCREEN_PROTECTED,
                            checked
                        )
                    }
                )
            }
        )

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = org.jetbrains.compose.resources.stringResource(Res.string.password),
            description = org.jetbrains.compose.resources.stringResource(Res.string.password_description),
            icon = if (securityPaneState.password.isEmpty()) Icons.Outlined.LockOpen
            else Icons.Outlined.Lock,
            trailingContent = {
                Switch(
                    checked = securityPaneState.password.isNotEmpty() || securityPaneState.isCreatingPass,
                    onCheckedChange = { checked ->
                        if (checked) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            mainViewModel.putPreferenceValue(
                                Constants.Preferences.IS_CREATING_PASSWORD,
                                true
                            )
                        } else {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            mainViewModel.putPreferenceValue(
                                Constants.Preferences.PASSWORD,
                                ""
                            )
                            mainViewModel.putPreferenceValue(
                                Constants.Preferences.IS_BIOMETRIC_ENABLED,
                                false
                            )
                            mainViewModel.putPreferenceValue(
                                Constants.Preferences.IS_CREATING_PASSWORD,
                                false
                            )
                        }
                    }
                )
            }
        )

        AnimatedVisibility(securityPaneState.password.isNotEmpty()) {
            DetailPaneItem(
                title = stringResource(R.string.biometric),
                icon = Icons.Outlined.Fingerprint,
                trailingContent = {
                    Switch(
                        checked = securityPaneState.isBiometricEnabled,
                        onCheckedChange = { checked ->
                            if (checked)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            else
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            val keyguardManager =
                                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                            if (keyguardManager.isKeyguardSecure) {
                                mainViewModel.putPreferenceValue(
                                    Constants.Preferences.IS_BIOMETRIC_ENABLED,
                                    checked
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.no_password_set),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
