package org.yangdai.kori.presentation.component.note.template

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.note.markdown.moveCursorLeftStateless
import org.yangdai.kori.presentation.component.note.markdown.moveCursorRightStateless

@Composable
fun Modifier.templateKeyEvents(textFieldState: TextFieldState): Modifier =
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            if (keyEvent.isCtrlPressed) {
                false
            } else {
                when (keyEvent.key) {

                    Key.DirectionLeft -> {
                        if (currentPlatformInfo.platform == Platform.Android) {
                            textFieldState.edit { moveCursorLeftStateless() }
                            true
                        } else false
                    }

                    Key.DirectionRight -> {
                        if (currentPlatformInfo.platform == Platform.Android) {
                            textFieldState.edit { moveCursorRightStateless() }
                            true
                        } else false
                    }

                    else -> false
                }
            }
        } else {
            false
        }
    }
