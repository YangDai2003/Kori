package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_info
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.Platform
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.setting.detail.AboutPane
import org.yangdai.kori.presentation.component.setting.detail.StylePane
import org.yangdai.kori.presentation.util.rememberCurrentPlatform
import org.yangdai.kori.presentation.viewModel.SettingsViewModel

@Composable
fun SettingsDetailPane(
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
    selectedItem: Int?,
    isExpanded: Boolean,
    navigateBackToList: () -> Unit
) {
    var topBarTitle by remember { mutableStateOf("") }
    SettingsDetailPaneContent(
        isExpanded = isExpanded,
        topBarTitle = topBarTitle,
        navigationIcon = {
            if (!isExpanded) {
                val platform = rememberCurrentPlatform()
                val icon = if (platform is Platform.IOS) Icons.Default.ArrowBackIosNew
                else Icons.AutoMirrored.Filled.ArrowBack
                IconButton(onClick = navigateBackToList) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        when (selectedItem) {
            null -> {}
            0 -> {
                topBarTitle = "Style"
                StylePane(viewModel)
            }
            3 -> {
                topBarTitle = stringResource(Res.string.app_info)
                AboutPane()
            }

            else -> {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailPaneContent(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    topBarTitle: String,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val platform = rememberCurrentPlatform()
    val showSmallTopAppBar by remember(platform, isExpanded) {
        derivedStateOf {
            platform is Platform.Desktop || isExpanded
        }
    }
    val scrollBehavior =
        if (showSmallTopAppBar) TopAppBarDefaults.pinnedScrollBehavior()
        else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollModifier =
        remember(scrollBehavior) { Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) }
    Scaffold(
        modifier = modifier.then(scrollModifier),
        topBar = {
            if (showSmallTopAppBar) {
                TopAppBar(
                    title = { PlatformStyleTopAppBarTitle(topBarTitle) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    windowInsets = TopAppBarDefaults.windowInsets.only(
                        WindowInsetsSides.Top + WindowInsetsSides.End
                    ),
                    colors = TopAppBarDefaults.topAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor),
                    scrollBehavior = scrollBehavior
                )
            } else {
                LargeTopAppBar(
                    title = { PlatformStyleTopAppBarTitle(topBarTitle) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor),
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        Box(
            Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection)
            ),
            content = content
        )
    }
}
