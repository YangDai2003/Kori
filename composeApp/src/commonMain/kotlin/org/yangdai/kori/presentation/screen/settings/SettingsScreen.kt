package org.yangdai.kori.presentation.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_info
import kori.composeapp.generated.resources.card
import kori.composeapp.generated.resources.cowriter
import kori.composeapp.generated.resources.data
import kori.composeapp.generated.resources.editor
import kori.composeapp.generated.resources.security
import kori.composeapp.generated.resources.settings
import kori.composeapp.generated.resources.style
import kori.composeapp.generated.resources.templates
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.setting.SettingsDetailPane
import org.yangdai.kori.presentation.component.setting.SettingsListPane

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun SettingsScreen(navigateUp: () -> Unit) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val coroutineScope = rememberCoroutineScope()
    val selectedItem = navigator.currentDestination?.contentKey
    val isExpanded by remember {
        derivedStateOf {
            navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded
                    && navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded
        }
    }

    BackHandler(enabled = !navigator.canNavigateBack()) {
        navigateUp()
    }

    key(navigator) {
        BackHandler(enabled = navigator.canNavigateBack()) {
            coroutineScope.launch {
                navigator.navigateBack()
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(0.95f)
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 8.dp,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                if (navigator.canNavigateBack()) {
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .minimumInteractiveComponentSize()
                            .size(IconButtonDefaults.extraSmallContainerSize(widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform)),
                        shape = IconButtonDefaults.extraSmallSquareShape,
                        onClick = {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                            imageVector = if (currentPlatformInfo.operatingSystem == OS.IOS || currentPlatformInfo.operatingSystem == OS.MACOS)
                                Icons.Default.ArrowBackIosNew
                            else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
                if (isExpanded) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(Res.string.settings),
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                } else {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        text = when (selectedItem) {
                            0 -> stringResource(Res.string.style)
                            1 -> stringResource(Res.string.editor)
                            2 -> stringResource(Res.string.card)
                            3 -> stringResource(Res.string.templates)
                            4 -> stringResource(Res.string.data)
                            5 -> stringResource(Res.string.security)
                            6 -> stringResource(Res.string.cowriter)
                            7 -> stringResource(Res.string.app_info)
                            else -> stringResource(Res.string.settings)
                        }
                    )
                }
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.extraSmallContainerSize(widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform)),
                    shape = IconButtonDefaults.extraSmallSquareShape,
                    onClick = navigateUp
                ) {
                    Icon(
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        SettingsListPane(selectedItem) { itemId ->
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    itemId
                                )
                            }
                        }
                    }
                },
                detailPane = {
                    AnimatedPane {
                        SettingsDetailPane(selectedItem, isExpanded)
                    }
                }
            )
        }
    }
}
