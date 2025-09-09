package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.note.moveCursorLeftStateless
import org.yangdai.kori.presentation.component.note.moveCursorRightStateless

fun Modifier.todoTextKeyEvents(textFieldState: TextFieldState): Modifier =
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {

                Key.DirectionLeft -> {
                    if (currentPlatformInfo.operatingSystem == OS.ANDROID) {
                        textFieldState.edit { moveCursorLeftStateless() }
                        true
                    } else false
                }

                Key.DirectionRight -> {
                    if (currentPlatformInfo.operatingSystem == OS.ANDROID) {
                        textFieldState.edit { moveCursorRightStateless() }
                        true
                    } else false
                }

                else -> false
            }
        } else {
            false
        }
    }
