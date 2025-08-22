package org.yangdai.kori.presentation.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.dialog.dialogShape
import org.yangdai.kori.presentation.component.setting.SettingsDetailPane
import org.yangdai.kori.presentation.component.setting.SettingsListPane
import kotlin.coroutines.cancellation.CancellationException

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

    var isVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val backProgress = remember { Animatable(0f) }
    val triggerExit = {
        coroutineScope.launch {
            isVisible = false
        }
    }
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300L)
            navigateUp()
        }
    }

    PredictiveBackHandler(enabled = !navigator.canNavigateBack()) { progress ->
        try {
            progress.collect { backEvent ->
                backProgress.snapTo(backEvent.progress)
            }
            triggerExit()
        } catch (_: CancellationException) {
            coroutineScope.launch {
                backProgress.animateTo(0f)
            }
        }
    }

    key(navigator) {
        BackHandler(enabled = navigator.canNavigateBack()) {
            coroutineScope.launch {
                navigator.navigateBack()
            }
        }
    }

    val windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val fraction = remember(windowSizeClass) {
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            0.9f
        } else if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            0.95f
        } else 1f
    }

    val size = LocalWindowInfo.current.containerSize
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn() + scaleIn(initialScale = 0.9f),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        Surface(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(fraction)
                .padding(16.dp)
                .graphicsLayer {
                    val progress = backProgress.value
                    translationY = progress * size.height * 0.5f
                    scaleX = 1f - (progress * 0.1f)
                    scaleY = 1f - (progress * 0.1f)
                },
            shape = dialogShape(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 8.dp,
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                Box(
                    Modifier.fillMaxWidth().padding(2.dp).pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (backProgress.value > 1f) {
                                        triggerExit()
                                    } else {
                                        backProgress.animateTo(0f)
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    backProgress.animateTo(0f)
                                }
                            }
                        ) { _, dragAmount ->
                            coroutineScope.launch {
                                // 根据 graphicsLayer 中的变换逻辑，反向计算出进度增量
                                // translationY = progress * size.height * 0.5f
                                // 因此，progress = translationY / (size.height * 0.5f)
                                // 进度增量 delta_progress = dragAmount / (size.height * 0.5f)
                                val progressDelta = dragAmount / (size.height * 0.5f)
                                // 将增量加到当前进度上，并确保进度不小于0（即不允许向上拖动使界面上移）
                                val newProgress =
                                    (backProgress.value + progressDelta).coerceAtLeast(0f)
                                backProgress.snapTo(newProgress)
                            }
                        }
                    }
                ) {
                    if (navigator.canNavigateBack()) {
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            shape = dialogShape(),
                            onClick = {
                                coroutineScope.launch {
                                    navigator.navigateBack()
                                }
                            }
                        ) {
                            Icon(
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
                            style = MaterialTheme.typography.titleLargeEmphasized
                        )
                    } else {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.titleLargeEmphasized,
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
                        modifier = Modifier.align(Alignment.CenterEnd),
                        shape = dialogShape(),
                        onClick = { triggerExit() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }
                ListDetailPaneScaffold(
                    directive = navigator.scaffoldDirective,
                    value = navigator.scaffoldValue,
                    listPane = {
                        AnimatedPane(Modifier.preferredWidth(320.dp)) {
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
                    },
                )
            }
        }
    }
}
