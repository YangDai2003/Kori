package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.password
import kori.composeapp.generated.resources.password_description
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.viewModel.SettingsViewModel

@Composable
actual fun SecurityPane(settingsViewModel: SettingsViewModel) {

    val securityPaneState by settingsViewModel.securityPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = if (securityPaneState.password.isEmpty()) Icons.Outlined.LockOpen
                    else Icons.Outlined.Lock,
                    contentDescription = null
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.password)) },
            trailingContent = {
                Switch(
                    checked = securityPaneState.password.isNotEmpty() || securityPaneState.isCreatingPass,
                    onCheckedChange = { checked ->
                        if (checked) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            if (securityPaneState.password.isEmpty()) {
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.IS_CREATING_PASSWORD,
                                    true
                                )
                            }
                        } else {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            settingsViewModel.putPreferenceValue(
                                Constants.Preferences.PASSWORD,
                                ""
                            )
                            settingsViewModel.putPreferenceValue(
                                Constants.Preferences.IS_BIOMETRIC_ENABLED,
                                false
                            )
                            settingsViewModel.putPreferenceValue(
                                Constants.Preferences.IS_CREATING_PASSWORD,
                                false
                            )
                        }
                    }
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(Res.string.password_description)
                )
            }
        )

        Spacer(Modifier.navigationBarsPadding())
    }
}
