package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TitleTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    initFocus: Boolean = false,
    focusManager: FocusManager = LocalFocusManager.current,
    onDone: () -> Unit = { focusManager.clearFocus() }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }

    BasicTextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (focused && !it.isFocused) onDone()
                focused = it.isFocused
            },
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        textStyle = MaterialTheme.typography.headlineSmallEmphasized.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        onKeyboardAction = { onDone() },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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

    LaunchedEffect(initFocus) { if (initFocus) focusRequester.requestFocus() }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TitleText(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClick: () -> Unit = {}
) {
    if (!visible) return

    val titleText = if (state.text.isNotEmpty()) {
        state.text.toString()
    } else {
        stringResource(Res.string.title)
    }

    val textColor = if (state.text.isNotEmpty()) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Text(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { onClick() }
        },
        text = titleText,
        style = MaterialTheme.typography.headlineSmallEmphasized.copy(
            color = textColor
        ),
        maxLines = 1,
    )
}