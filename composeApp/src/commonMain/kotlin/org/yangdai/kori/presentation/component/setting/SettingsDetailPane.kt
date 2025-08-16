package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_info
import kori.composeapp.generated.resources.card
import kori.composeapp.generated.resources.cowriter
import kori.composeapp.generated.resources.data
import kori.composeapp.generated.resources.editor
import kori.composeapp.generated.resources.security
import kori.composeapp.generated.resources.style
import kori.composeapp.generated.resources.templates
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBar
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarNavigationIcon
import org.yangdai.kori.presentation.component.PlatformStyleTopAppBarTitle
import org.yangdai.kori.presentation.component.PlatformTopAppBarType
import org.yangdai.kori.presentation.component.rememberPlatformStyleTopAppBarState
import org.yangdai.kori.presentation.component.setting.detail.AboutPane
import org.yangdai.kori.presentation.component.setting.detail.AiPane
import org.yangdai.kori.presentation.component.setting.detail.CardPane
import org.yangdai.kori.presentation.component.setting.detail.DataPane
import org.yangdai.kori.presentation.component.setting.detail.EditorPane
import org.yangdai.kori.presentation.component.setting.detail.SecurityPane
import org.yangdai.kori.presentation.component.setting.detail.StylePane
import org.yangdai.kori.presentation.component.setting.detail.TemplatePane
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel

@Composable
fun SettingsDetailPane(
    viewModel: SettingsViewModel = koinViewModel(),
    selectedItem: Int?,
    isExpanded: Boolean,
    navigateBackToList: () -> Unit
) {
    var topBarTitle by remember { mutableStateOf("") }
    SettingsDetailPaneContent(
        isExpanded = isExpanded,
        topBarTitle = topBarTitle,
        navigationIcon = { if (!isExpanded) PlatformStyleTopAppBarNavigationIcon(navigateBackToList) }
    ) {
        when (selectedItem) {
            0 -> {
                topBarTitle = stringResource(Res.string.style)
                StylePane(viewModel)
            }

            1 -> {
                topBarTitle = stringResource(Res.string.editor)
                EditorPane(viewModel)
            }

            2 -> {
                topBarTitle = stringResource(Res.string.card)
                CardPane(viewModel)
            }

            3 -> {
                topBarTitle = stringResource(Res.string.templates)
                TemplatePane(viewModel)
            }

            4 -> {
                topBarTitle = stringResource(Res.string.data)
                DataPane()
            }

            5 -> {
                topBarTitle = stringResource(Res.string.security)
                SecurityPane(viewModel)
            }

            6 -> {
                topBarTitle = stringResource(Res.string.cowriter)
                AiPane(viewModel)
            }

            7 -> {
                topBarTitle = stringResource(Res.string.app_info)
                AboutPane()
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDetailPaneContent(
    isExpanded: Boolean,
    topBarTitle: String,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val topAppBarState = rememberPlatformStyleTopAppBarState()
    val windowInsets = if (topAppBarState.type == PlatformTopAppBarType.SmallPinned)
        TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.End)
    else TopAppBarDefaults.windowInsets
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarState.scrollBehavior.nestedScrollConnection),
        topBar = {
            PlatformStyleTopAppBar(
                state = topAppBarState,
                title = { PlatformStyleTopAppBarTitle(topBarTitle) },
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        Box(
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = if (isExpanded) RoundedCornerShape(topStart = 12.dp)
                    else RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                Modifier.widthIn(max = 600.dp).fillMaxSize()
                    .padding(end = innerPadding.calculateEndPadding(layoutDirection))
            ) {
                content()
            }
        }
    }
}
