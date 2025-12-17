package org.yangdai.kori.presentation.component.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import kori.composeapp.generated.resources.create_password
import kori.composeapp.generated.resources.enter_again
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.sin

private const val TOTAL_PASSWORD_LENGTH = 6

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NumberLockScreen(
    storedPassword: String,
    isCreatingPassword: Boolean,
    onCreatingCanceled: () -> Unit,
    onPassCreated: (String) -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    isBiometricAuthEnabled: Boolean = false,
    onBiometricClick: () -> Unit = {}
) = LoginDialog {
    val hapticFeedback = LocalHapticFeedback.current
    var inputPassword by remember { mutableStateOf("") }
    var inputPassword2 by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    BackHandler { }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { }
            .focusRequester(focusRequester) // 关联 FocusRequester
            .onKeyEvent { keyEvent ->
                // 只在按键抬起时处理，防止按住按键时重复触发
                if (keyEvent.type != KeyEventType.KeyUp) {
                    return@onKeyEvent false
                }

                val number = when (keyEvent.key) {
                    Key.One, Key.NumPad1 -> "1"
                    Key.Two, Key.NumPad2 -> "2"
                    Key.Three, Key.NumPad3 -> "3"
                    Key.Four, Key.NumPad4 -> "4"
                    Key.Five, Key.NumPad5 -> "5"
                    Key.Six, Key.NumPad6 -> "6"
                    Key.Seven, Key.NumPad7 -> "7"
                    Key.Eight, Key.NumPad8 -> "8"
                    Key.Nine, Key.NumPad9 -> "9"
                    Key.Zero, Key.NumPad0 -> "0"
                    else -> null
                }

                // 如果是数字键
                if (number != null) {
                    // 复用现有的密码输入逻辑
                    handlePasswordInput(
                        number = number,
                        isCreatingPassword = isCreatingPassword,
                        inputPassword = inputPassword,
                        inputPassword2 = inputPassword2,
                        storedPassword = storedPassword,
                        hapticFeedback = hapticFeedback,
                        onPasswordChanged = { newPassword -> inputPassword = newPassword },
                        onPassword2Changed = { newPassword2 -> inputPassword2 = newPassword2 },
                        onError = { isError = true },
                        onPassCreated = onPassCreated,
                        onAuthenticated = onAuthenticated
                    )
                    return@onKeyEvent true // 事件已处理
                }

                // 如果是删除键
                if (keyEvent.key == Key.Backspace || keyEvent.key == Key.Delete) {
                    // 复用现有的删除逻辑
                    handleDeleteClick(
                        isCreatingPassword = isCreatingPassword,
                        inputPassword = inputPassword,
                        inputPassword2 = inputPassword2,
                        onPasswordChanged = { newPassword -> inputPassword = newPassword },
                        onPassword2Changed = { newPassword2 -> inputPassword2 = newPassword2 }
                    )
                    return@onKeyEvent true // 事件已处理
                }

                false // 其他按键不处理
            }
            .then(
                if (isCreatingPassword) Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                else modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight

        // 计算适合当前屏幕的按钮尺寸
        val buttonSize = calculateButtonSize(isLandscape, screenWidth, screenHeight)
        val padding = buttonSize.times(0.25f)

        // 处理错误动画
        val offsetX = handleErrorAnimation(isError) {
            isError = false
            inputPassword = ""
            inputPassword2 = ""
        }

        // 根据屏幕方向选择不同布局
        if (isLandscape) {
            LandscapeLayout(
                isCreatingPassword = isCreatingPassword,
                inputPassword = inputPassword,
                inputPassword2 = inputPassword2,
                offsetX = offsetX,
                isError = isError,
                buttonSize = buttonSize,
                padding = padding,
                isBiometricAuthEnabled = isBiometricAuthEnabled,
                onNumberClick = { number ->
                    handlePasswordInput(
                        number = number,
                        isCreatingPassword = isCreatingPassword,
                        inputPassword = inputPassword,
                        inputPassword2 = inputPassword2,
                        storedPassword = storedPassword,
                        hapticFeedback = hapticFeedback,
                        onPasswordChanged = { newPassword -> inputPassword = newPassword },
                        onPassword2Changed = { newPassword2 -> inputPassword2 = newPassword2 },
                        onError = { isError = true },
                        onPassCreated = onPassCreated,
                        onAuthenticated = onAuthenticated
                    )
                },
                onDeleteClick = {
                    handleDeleteClick(
                        isCreatingPassword = isCreatingPassword,
                        inputPassword = inputPassword,
                        inputPassword2 = inputPassword2,
                        onPasswordChanged = { newPassword -> inputPassword = newPassword },
                        onPassword2Changed = { newPassword2 -> inputPassword2 = newPassword2 }
                    )
                },
                onBiometricClick = onBiometricClick,
                onCreatingCanceled = onCreatingCanceled
            )
        } else {
            PortraitLayout(
                isCreatingPassword = isCreatingPassword,
                inputPassword = inputPassword,
                inputPassword2 = inputPassword2,
                offsetX = offsetX,
                isError = isError,
                buttonSize = buttonSize,
                padding = padding,
                isBiometricAuthEnabled = isBiometricAuthEnabled,
                onNumberClick = { number ->
                    handlePasswordInput(
                        number = number,
                        isCreatingPassword = isCreatingPassword,
                        inputPassword = inputPassword,
                        inputPassword2 = inputPassword2,
                        storedPassword = storedPassword,
                        hapticFeedback = hapticFeedback,
                        onPasswordChanged = { newPassword -> inputPassword = newPassword },
                        onPassword2Changed = { newPassword2 -> inputPassword2 = newPassword2 },
                        onError = { isError = true },
                        onPassCreated = onPassCreated,
                        onAuthenticated = onAuthenticated
                    )
                },
                onDeleteClick = {
                    handleDeleteClick(
                        isCreatingPassword = isCreatingPassword,
                        inputPassword = inputPassword,
                        inputPassword2 = inputPassword2,
                        onPasswordChanged = { newPassword -> inputPassword = newPassword },
                        onPassword2Changed = { newPassword2 -> inputPassword2 = newPassword2 }
                    )
                },
                onBiometricClick = onBiometricClick,
                onCreatingCanceled = onCreatingCanceled
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
expect fun LoginDialog(content: @Composable () -> Unit)

@Composable
private fun calculateButtonSize(isLandscape: Boolean, screenWidth: Dp, screenHeight: Dp): Dp =
    if (isLandscape) {
        min(screenHeight.times(0.15f), 80.dp)
    } else {
        min(screenWidth.times(0.2f), 80.dp)
    }

@Composable
private fun handleErrorAnimation(
    isError: Boolean,
    onFinished: () -> Unit
): Float {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isError) 1f else 0f,
        animationSpec = repeatable(
            iterations = 2,
            animation = tween(200),
            repeatMode = RepeatMode.Reverse
        ),
        finishedListener = { onFinished() },
        label = "shakeProgress"
    )

    return remember(animatedProgress) {
        if (isError) {
            sin(animatedProgress * 2 * PI.toFloat()) * 30f
        } else {
            0f
        }
    }
}

private fun handlePasswordInput(
    number: String,
    isCreatingPassword: Boolean,
    inputPassword: String,
    inputPassword2: String,
    storedPassword: String,
    hapticFeedback: HapticFeedback,
    onPasswordChanged: (String) -> Unit,
    onPassword2Changed: (String) -> Unit,
    onError: () -> Unit,
    onPassCreated: (String) -> Unit,
    onAuthenticated: () -> Unit
) {
    if (isCreatingPassword) {
        if (inputPassword.length < TOTAL_PASSWORD_LENGTH) {
            onPasswordChanged(inputPassword + number)
        } else if (inputPassword2.length < TOTAL_PASSWORD_LENGTH) {
            val newPassword2 = inputPassword2 + number
            onPassword2Changed(newPassword2)

            if (newPassword2.length == TOTAL_PASSWORD_LENGTH) {
                if (inputPassword == newPassword2) {
                    onPassCreated(inputPassword)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                    onError()
                }
            }
        }
    } else {
        if (inputPassword.length < TOTAL_PASSWORD_LENGTH) {
            val newPassword = inputPassword + number
            onPasswordChanged(newPassword)

            if (newPassword.length == TOTAL_PASSWORD_LENGTH) {
                if (newPassword == storedPassword) {
                    onAuthenticated()
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                    onError()
                }
            }
        }
    }
}

private fun handleDeleteClick(
    isCreatingPassword: Boolean,
    inputPassword: String,
    inputPassword2: String,
    onPasswordChanged: (String) -> Unit,
    onPassword2Changed: (String) -> Unit
) {
    if (isCreatingPassword) {
        if (inputPassword.length < TOTAL_PASSWORD_LENGTH && inputPassword.isNotEmpty()) {
            onPasswordChanged(inputPassword.dropLast(1))
        } else if (inputPassword2.isNotEmpty()) {
            onPassword2Changed(inputPassword2.dropLast(1))
        }
    } else if (inputPassword.isNotEmpty()) {
        onPasswordChanged(inputPassword.dropLast(1))
    }
}

@Composable
fun LandscapeLayout(
    isCreatingPassword: Boolean,
    inputPassword: String,
    inputPassword2: String,
    offsetX: Float,
    isError: Boolean,
    buttonSize: Dp,
    padding: Dp,
    isBiometricAuthEnabled: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onCreatingCanceled: () -> Unit
) = Row(
    modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
) {
    // 左侧: 标题和密码显示区域
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PasswordHeaderText(isCreatingPassword, inputPassword)
        Spacer(modifier = Modifier.height(32.dp))
        PasswordCircles(
            modifier = Modifier.graphicsLayer { translationX = offsetX },
            currentPasswordLength = getCurrentPasswordLength(
                isCreatingPassword,
                inputPassword,
                inputPassword2
            ),
            isError = isError
        )
        if (isCreatingPassword) {
            Spacer(modifier = Modifier.height(padding))
            TextButton(onClick = onCreatingCanceled) {
                Text(stringResource(Res.string.cancel))
            }
        }
    }

    // 右侧: 数字键盘
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NumberPad(
            biometricAuthEnabled = isBiometricAuthEnabled,
            size = buttonSize,
            padding = padding,
            onNumberClick = onNumberClick,
            onDeleteClick = onDeleteClick,
            onBiometricClick = onBiometricClick
        )
    }
}

@Composable
fun PortraitLayout(
    isCreatingPassword: Boolean,
    inputPassword: String,
    inputPassword2: String,
    offsetX: Float,
    isError: Boolean,
    buttonSize: Dp,
    padding: Dp,
    isBiometricAuthEnabled: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onCreatingCanceled: () -> Unit
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceEvenly
) {
    // 顶部区域: 标题和密码显示
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PasswordHeaderText(isCreatingPassword, inputPassword)
        Spacer(modifier = Modifier.height(32.dp))
        PasswordCircles(
            modifier = Modifier.graphicsLayer { translationX = offsetX },
            currentPasswordLength = getCurrentPasswordLength(
                isCreatingPassword,
                inputPassword,
                inputPassword2
            ),
            isError = isError
        )
    }

    // 底部区域: 数字键盘
    Column(
        modifier = Modifier.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NumberPad(
            biometricAuthEnabled = isBiometricAuthEnabled,
            size = buttonSize,
            padding = padding,
            onNumberClick = onNumberClick,
            onDeleteClick = onDeleteClick,
            onBiometricClick = onBiometricClick
        )
        if (isCreatingPassword) {
            Spacer(modifier = Modifier.height(padding))
            TextButton(onClick = onCreatingCanceled) {
                Text(stringResource(Res.string.cancel))
            }
        }
    }
}

@Composable
private fun PasswordHeaderText(isCreatingPassword: Boolean, inputPassword: String) {
    if (isCreatingPassword) {
        Text(
            text = if (inputPassword.length < TOTAL_PASSWORD_LENGTH)
                stringResource(Res.string.create_password)
            else
                stringResource(Res.string.enter_again),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    } else {
        LogoText()
    }
}

private fun getCurrentPasswordLength(
    isCreatingPassword: Boolean,
    inputPassword: String,
    inputPassword2: String
): Int {
    return if (isCreatingPassword) {
        if (inputPassword.length < TOTAL_PASSWORD_LENGTH) inputPassword.length else inputPassword2.length
    } else {
        inputPassword.length
    }
}

@Composable
fun PasswordCircles(
    modifier: Modifier = Modifier,
    currentPasswordLength: Int,
    isError: Boolean
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
) {
    repeat(TOTAL_PASSWORD_LENGTH) { index ->
        Circle(
            isFilled = index < currentPasswordLength,
            isError = isError
        )
    }
}

@Composable
fun Circle(
    isFilled: Boolean,
    isError: Boolean
) {
    val circleColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.errorContainer
            isFilled -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        label = "circleColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        label = "borderColor"
    )

    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(circleColor)
            .border(2.dp, borderColor, CircleShape)
    )
}

@Composable
fun NumberPad(
    size: Dp,
    padding: Dp,
    biometricAuthEnabled: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(padding)
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    numbers.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(padding, Alignment.CenterHorizontally)
        ) {
            row.forEach { number ->
                NumberButton(size, number, onNumberClick)
            }
        }
    }

    // 底部按钮行 (指纹/空白, 0, 删除)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(padding, Alignment.CenterHorizontally)
    ) {
        if (biometricAuthEnabled) {
            IconButton(
                size = size,
                iconSize = 30.dp,
                imageVector = Icons.Filled.Fingerprint,
                onClick = onBiometricClick
            )
        } else {
            Spacer(modifier = Modifier.size(size))
        }

        NumberButton(size, "0", onNumberClick)

        IconButton(
            size = size,
            iconSize = 24.dp,
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            onClick = onDeleteClick
        )
    }
}

@Composable
fun NumberButton(
    size: Dp,
    number: String,
    onNumberClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceBright,
        label = "buttonColor"
    )

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        label = "cornerRadius"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                shape = RoundedCornerShape(cornerPercent)
                clip = true
            }
            .background(animatedColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onNumberClick(number)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    size: Dp,
    iconSize: Dp,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.size(iconSize)
        )
    }
}
