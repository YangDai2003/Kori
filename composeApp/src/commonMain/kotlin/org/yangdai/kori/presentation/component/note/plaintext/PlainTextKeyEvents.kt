package org.yangdai.kori.presentation.component.note.plaintext

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
import org.yangdai.kori.currentPlatform
import org.yangdai.kori.presentation.component.note.markdown.moveCursorLeftStateless
import org.yangdai.kori.presentation.component.note.markdown.moveCursorRightStateless

@Composable
fun Modifier.plainTextKeyEvents(textFieldState: TextFieldState): Modifier =
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            if (keyEvent.isCtrlPressed) {
                false
            } else {
                when (keyEvent.key) {

                    Key.DirectionLeft -> {
                        if (currentPlatform() == Platform.Android) {
                            textFieldState.edit { moveCursorLeftStateless() }
                            true
                        } else false
                    }

                    Key.DirectionRight -> {
                        if (currentPlatform() == Platform.Android) {
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
