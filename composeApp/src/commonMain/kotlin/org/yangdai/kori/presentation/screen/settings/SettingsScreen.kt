package org.yangdai.kori.presentation.screen.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.dialog.dialogShape
import org.yangdai.kori.presentation.component.setting.SettingsDetailPane
import org.yangdai.kori.presentation.component.setting.SettingsListPane
import org.yangdai.kori.presentation.screen.main.MainViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SettingsScreen(mainViewModel: MainViewModel, navigateUp: () -> Unit) {
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
    LaunchedEffect(Unit) { isVisible = true }
    val visibilityProgress = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            visibilityProgress.animateTo(
                1f,
                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
            )
        } else {
            visibilityProgress.animateTo(
                0f,
                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
            )
            navigateUp()
        }
    }

    val backProgress = remember { Animatable(0f) }
    PredictiveBackHandler(enabled = !navigator.canNavigateBack()) { progress ->
        try {
            progress.collect { backEvent ->
                backProgress.snapTo(backEvent.progress)
            }
            isVisible = false
        } catch (_: CancellationException) {
            coroutineScope.launch {
                backProgress.animateTo(0f)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val fillMaxWidthFraction = when {
            maxWidth >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND.dp -> 0.8f
            maxWidth >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND.dp -> 0.85f
            maxWidth >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND.dp -> 0.9f
            maxWidth >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp -> 0.95f
            else -> 1f
        }
        val fillMaxHeightFraction = when {
            maxHeight >= WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND.dp -> 0.9f
            maxHeight >= WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.dp -> 0.95f
            else -> 1f
        }
        val dialogShape = dialogShape()
        Box(
            modifier = Modifier
                .fillMaxWidth(fillMaxWidthFraction)
                .fillMaxHeight(fillMaxHeightFraction)
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer {
                    val backProgress = backProgress.value
                    val visProgress = visibilityProgress.value

                    translationY =
                        backProgress * maxHeight.toPx() * 0.5f +
                                (1 - visibilityProgress.value) * maxHeight.toPx()
                    scaleX = (1f - (backProgress * 0.1f)) * (0.9f + 0.1f * visProgress)
                    scaleY = (1f - (backProgress * 0.1f)) * (0.9f + 0.1f * visProgress)
//                    alpha = visProgress
                    shadowElevation = 8f
                    shape = dialogShape
                    clip = true
                }
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, dialogShape),
            contentAlignment = Alignment.TopCenter
        ) {
            SettingsListDetailScaffold(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                navigator = navigator,
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
                        SettingsDetailPane(selectedItem, isExpanded, mainViewModel)
                    }
                }
            )
            Box(
                Modifier.fillMaxWidth().padding(2.dp).pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (backProgress.value > 1f) {
                                    isVisible = false
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
                            val progressDelta =
                                dragAmount / (this@BoxWithConstraints.maxHeight.toPx() * 0.5f)
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
                        shape = dialogShape,
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
                        modifier = Modifier.align(Alignment.CenterStart).width(320.dp),
                        text = stringResource(Res.string.settings),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        textAlign = TextAlign.Center
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
                    shape = dialogShape,
                    onClick = { isVisible = false }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
expect fun SettingsListDetailScaffold(
    modifier: Modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.surfaceContainerLow),
    navigator: ThreePaneScaffoldNavigator<Int>,
    listPane: @Composable ThreePaneScaffoldPaneScope.() -> Unit,
    detailPane: @Composable ThreePaneScaffoldPaneScope.() -> Unit
)
