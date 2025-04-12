package org.yangdai.kori

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.always_on_top
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.back
import kori.composeapp.generated.resources.close
import kori.composeapp.generated.resources.keep
import kori.composeapp.generated.resources.keep_off
import kori.composeapp.generated.resources.maximize
import kori.composeapp.generated.resources.minimize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.theme.KoriTheme
import java.awt.Dimension

fun main() {
    KoinInitializer.init()
    application {
        val state = rememberWindowState()
        val isWindowFloating by remember(state) {
            derivedStateOf {
                state.placement == WindowPlacement.Floating
            }
        }
        var alwaysOnTop by rememberSaveable { mutableStateOf(false) }
        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = stringResource(Res.string.app_name),
            undecorated = true,
            transparent = true,
            alwaysOnTop = alwaysOnTop
        ) {
            window.minimumSize = Dimension(300, 300)
            KoriTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    shape = if (isWindowFloating) RoundedCornerShape(8.dp) else RectangleShape,
                    shadowElevation = 2.dp
                ) {
                    Column {
                        val navController = rememberNavController()
                        val navState = navController.currentBackStackEntryAsState()
                        val canNavigateUp by remember(navState.value) {
                            derivedStateOf {
                                navController.previousBackStackEntry != null
                            }
                        }

                        WindowDraggableArea(
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        state.placement =
                                            if (isWindowFloating) WindowPlacement.Maximized else WindowPlacement.Floating
                                    }
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TooltipIconButton(
                                    buttonModifier = Modifier.padding(4.dp).size(32.dp),
                                    iconModifier = Modifier.size(20.dp),
                                    tipText = stringResource(Res.string.back),
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    onClick = { navController.navigateUp() },
                                    enabled = canNavigateUp
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    TooltipIconButton(
                                        buttonModifier = Modifier.padding(4.dp).size(32.dp),
                                        iconModifier = Modifier.size(20.dp),
                                        tipText = stringResource(Res.string.always_on_top),
                                        icon = if (!alwaysOnTop) painterResource(Res.drawable.keep)
                                        else painterResource(Res.drawable.keep_off),
                                        onClick = {
                                            alwaysOnTop = !alwaysOnTop
                                        }
                                    )

                                    TooltipIconButton(
                                        buttonModifier = Modifier.padding(4.dp).size(32.dp),
                                        iconModifier = Modifier.size(20.dp),
                                        tipText = stringResource(Res.string.minimize),
                                        icon = Icons.Default.Remove,
                                        onClick = {
                                            state.isMinimized = !state.isMinimized
                                        }
                                    )

                                    TooltipIconButton(
                                        buttonModifier = Modifier.padding(4.dp).size(32.dp),
                                        iconModifier = Modifier.size(20.dp),
                                        tipText = stringResource(Res.string.maximize),
                                        icon = if (isWindowFloating) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                                        onClick = {
                                            state.placement =
                                                if (isWindowFloating) WindowPlacement.Maximized else WindowPlacement.Floating
                                        }
                                    )

                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isHovered by interactionSource.collectIsHoveredAsState()

                                    TooltipIconButton(
                                        buttonModifier = Modifier.padding(4.dp).size(32.dp),
                                        iconModifier = Modifier.size(20.dp),
                                        tipText = stringResource(Res.string.close),
                                        icon = Icons.Default.Close,
                                        onClick = ::exitApplication,
                                        colors = IconButtonDefaults.iconButtonColors()
                                            .copy(
                                                containerColor = if (isHovered) Color.Red.copy(alpha = 0.6f)
                                                else IconButtonDefaults.iconButtonColors().containerColor
                                            ),
                                        interactionSource = interactionSource
                                    )
                                }
                            }
                        }
                        App(navController)
                    }
                }
            }
        }
    }
}