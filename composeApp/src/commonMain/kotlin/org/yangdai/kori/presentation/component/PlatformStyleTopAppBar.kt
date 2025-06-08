package org.yangdai.kori.presentation.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatformInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformStyleTopAppBar(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) = if (currentPlatformInfo.platform == Platform.Desktop) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
} else {
    LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun PlatformStyleTopAppBarNavigationIcon(onClick: () -> Unit) =
    TooltipIconButton(
        tipText = stringResource(Res.string.back),
        icon = if (currentPlatformInfo.operatingSystem == OS.IOS || currentPlatformInfo.operatingSystem == OS.MACOS) Icons.Default.ArrowBackIosNew
        else Icons.AutoMirrored.Filled.ArrowBack,
        onClick = onClick
    )

@Composable
fun PlatformStyleTopAppBarTitle(title: String) =
    Text(
        text = title,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )