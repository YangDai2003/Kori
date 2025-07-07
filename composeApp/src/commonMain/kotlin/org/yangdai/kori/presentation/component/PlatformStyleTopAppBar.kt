package org.yangdai.kori.presentation.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.util.isScreenSizeLarge

enum class PlatformTopAppBarType {
    Small, Centered, Large
}

data class PlatformStyleTopAppBarState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val type: PlatformTopAppBarType,
    val scrollBehavior: TopAppBarScrollBehavior
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPlatformStyleTopAppBarState(): PlatformStyleTopAppBarState {
    val type = when {
        currentPlatformInfo.isDesktop() || isScreenSizeLarge() -> PlatformTopAppBarType.Small
        currentPlatformInfo.operatingSystem == OS.IOS -> PlatformTopAppBarType.Centered
        else -> PlatformTopAppBarType.Large
    }
    val scrollBehavior =
        when (type) {
            PlatformTopAppBarType.Small, PlatformTopAppBarType.Centered -> TopAppBarDefaults.pinnedScrollBehavior()
            PlatformTopAppBarType.Large -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        }
    return remember(type, scrollBehavior) {
        PlatformStyleTopAppBarState(type, scrollBehavior)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformStyleTopAppBar(
    state: PlatformStyleTopAppBarState,
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) = when (state.type) {
    PlatformTopAppBarType.Small -> TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        expandedHeight = 56.dp,
        scrollBehavior = state.scrollBehavior
    )

    PlatformTopAppBarType.Centered -> CenterAlignedTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        expandedHeight = 52.dp,
        scrollBehavior = state.scrollBehavior
    )

    PlatformTopAppBarType.Large -> LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = state.scrollBehavior
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