package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.password
import kori.composeapp.generated.resources.password_description
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
actual fun SecurityPane(settingsViewModel: SettingsViewModel) {

    val securityPaneState by settingsViewModel.securityPaneState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(Modifier.height(16.dp))

        DetailPaneItem(
            title = stringResource(Res.string.password),
            description = stringResource(Res.string.password_description),
            icon = if (securityPaneState.password.isEmpty()) Icons.Outlined.LockOpen
            else Icons.Outlined.Lock,
            trailingContent = {
                Switch(
                    checked = securityPaneState.password.isNotEmpty() || securityPaneState.isCreatingPass,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (securityPaneState.password.isEmpty()) {
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.IS_CREATING_PASSWORD,
                                    true
                                )
                            }
                        } else {
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
            }
        )

        Spacer(Modifier.navigationBarsPadding())
    }
}
