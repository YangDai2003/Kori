package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PermDeviceInformation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_info
import kori.composeapp.generated.resources.color_platte
import kori.composeapp.generated.resources.dark_mode
import kori.composeapp.generated.resources.guide
import kori.composeapp.generated.resources.password
import kori.composeapp.generated.resources.privacy_policy
import kori.composeapp.generated.resources.security
import kori.composeapp.generated.resources.settings
import kori.composeapp.generated.resources.style
import kori.composeapp.generated.resources.version
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.Platform
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBar
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.util.rememberCurrentPlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListPane(
    navigateUp: () -> Unit,
    navigateToDetail: (Int) -> Unit
) {
    val platform = rememberCurrentPlatform()
    val scrollBehavior = if (platform is Platform.Desktop) TopAppBarDefaults.pinnedScrollBehavior()
    else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            PlatformStyleTopAppBar(
                title = { PlatformStyleTopAppBarTitle(stringResource(Res.string.settings)) },
                navigationIcon = { PlatformStyleTopAppBarNavigationIcon(onClick = navigateUp) },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection)
                )
                .verticalScroll(rememberScrollState())
        ) {
            SettingsListSection(
                {
                    SettingsListItem(
                        title = stringResource(Res.string.style),
                        description = stringResource(Res.string.dark_mode)
                                + "  •  " + stringResource(Res.string.color_platte),
                        icon = Icons.Outlined.Edit,
                        onClick = { navigateToDetail(0) }
                    )
                },
                {
                    SettingsListItem(
                        title = "Item 2",
                        description = "Description for item 2",
                        icon = Icons.Outlined.Edit,
                        onClick = { navigateToDetail(1) }
                    )
                },
                {
                    SettingsListItem(
                        title = "Item 3",
                        description = "Description for item 3",
                        icon = Icons.Outlined.Edit,
                        onClick = { navigateToDetail(2) }
                    )
                }
            )
            SettingsListSection(
                {
                    SettingsListItem(
                        title = stringResource(Res.string.security),
                        description = stringResource(Res.string.password),
                        icon = Icons.Outlined.Security,
                        onClick = { navigateToDetail(4) }
                    )
                }
            )
            SettingsListSection(
                {
                    SettingsListItem(
                        title = stringResource(Res.string.app_info),
                        description = stringResource(Res.string.version)
                                + " • " + stringResource(Res.string.guide)
                                + " • " + stringResource(Res.string.privacy_policy),
                        icon = Icons.Outlined.PermDeviceInformation,
                        onClick = { navigateToDetail(3) }
                    )
                }
            )
        }
    }
}
