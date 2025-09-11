package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.title
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TitleTextField(
    state: TextFieldState,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = BasicTextField(
    modifier = modifier.onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {
                Key.DirectionLeft -> {
                    if (currentPlatformInfo.operatingSystem == OS.ANDROID) {
                        state.edit { moveCursorLeftStateless() }
                        true
                    } else false
                }

                Key.DirectionRight -> {
                    if (currentPlatformInfo.operatingSystem == OS.ANDROID) {
                        state.edit { moveCursorRightStateless() }
                        true
                    } else false
                }

                else -> false
            }
        } else {
            false
        }
    },
    state = state,
    lineLimits = TextFieldLineLimits.SingleLine,
    textStyle = MaterialTheme.typography.headlineSmallEmphasized.copy(
        color = MaterialTheme.colorScheme.onSurface
    ),
    readOnly = readOnly,
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    interactionSource = interactionSource,
    decorator = { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = state.text.toString(),
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = stringResource(Res.string.title),
                    style = MaterialTheme.typography.headlineSmallEmphasized.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            },
            contentPadding = PaddingValues(0.dp),
            container = {}
        )
    }
)