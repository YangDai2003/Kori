package org.yangdai.kori

import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.KeyboardShortcutInfo
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.screen.LoginOverlayScreen
import org.yangdai.kori.presentation.state.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.viewModel.SettingsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
            val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
            KoriTheme(
                darkMode = if (stylePaneState.theme == AppTheme.SYSTEM) isSystemInDarkTheme()
                else stylePaneState.theme == AppTheme.DARK,
                color = stylePaneState.color,
                amoledMode = stylePaneState.isAppInAmoledMode
            ) {
                Surface {
                    var isLocked by rememberSaveable { mutableStateOf(true) }
                    var biometricAuthEnabled by rememberSaveable { mutableStateOf(true) }
                    val blur by animateDpAsState(
                        targetValue = if (isLocked) 16.dp else 0.dp,
                        label = "Blur"
                    )
                    AppNavHost(modifier = Modifier.blur(blur))
                    AnimatedVisibility(visible = isLocked, enter = fadeIn(), exit = fadeOut()) {
                        LoginOverlayScreen(
                            storedPassword = "123456",
                            biometricAuthEnabled = biometricAuthEnabled,
                            isCreatingPassword = false,
                            onCreatingCanceled = {

                            },
                            onPassCreated = {

                            },
                            onAuthenticated = {
                                isLocked = false
                            },
                            onAuthenticationNotEnrolled = {
                                biometricAuthEnabled = false
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onProvideKeyboardShortcuts(
        data: MutableList<KeyboardShortcutGroup>?, menu: Menu?, deviceId: Int
    ) {

        val noteEdit = KeyboardShortcutGroup(
            getString(R.string.note_editing), listOf(
                KeyboardShortcutInfo(
                    getString(android.R.string.selectAll), KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(android.R.string.cut), KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(android.R.string.copy), KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(android.R.string.paste), KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.undo), KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.redo), KeyEvent.KEYCODE_Y, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.bold), KeyEvent.KEYCODE_B, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.italic), KeyEvent.KEYCODE_I, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.underline), KeyEvent.KEYCODE_U, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.strikethrough), KeyEvent.KEYCODE_D, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.mark), KeyEvent.KEYCODE_M, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.table), KeyEvent.KEYCODE_T, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.link), KeyEvent.KEYCODE_K, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.preview), KeyEvent.KEYCODE_P, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.find), KeyEvent.KEYCODE_F, KeyEvent.META_CTRL_ON
                ),


                KeyboardShortcutInfo(
                    getString(R.string.list),
                    KeyEvent.KEYCODE_L,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.scan),
                    KeyEvent.KEYCODE_S,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.code),
                    KeyEvent.KEYCODE_K,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.quote),
                    KeyEvent.KEYCODE_Q,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.math),
                    KeyEvent.KEYCODE_M,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.horizontal_rule),
                    KeyEvent.KEYCODE_R,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.task_list),
                    KeyEvent.KEYCODE_T,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.mermaid_diagram),
                    KeyEvent.KEYCODE_D,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.image),
                    KeyEvent.KEYCODE_I,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.templates),
                    KeyEvent.KEYCODE_P,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                )
            )
        )

        data?.add(noteEdit)
    }
}
