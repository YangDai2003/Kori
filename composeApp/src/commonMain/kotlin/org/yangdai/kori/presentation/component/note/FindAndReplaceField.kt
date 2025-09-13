package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.find
import kori.composeapp.generated.resources.replace
import kori.composeapp.generated.resources.replace_all
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.util.isScreenWidthExpanded

@Stable
class FindAndReplaceState {
    var searchWord by mutableStateOf("")
    var replaceWord by mutableStateOf("")
    var position by mutableStateOf("")
    var scrollDirection by mutableStateOf(ScrollDirection.NONE)
    var replaceType by mutableStateOf(ReplaceType.NONE)

    companion object {
        val Saver: Saver<FindAndReplaceState, Triple<String, String, String>> = Saver(
            save = { state -> Triple(state.searchWord, state.replaceWord, state.position) },
            restore = { data ->
                val (searchWord, replaceWord, position) = data
                FindAndReplaceState().apply {
                    this.searchWord = searchWord
                    this.replaceWord = replaceWord
                    this.position = position
                    this.scrollDirection = ScrollDirection.NONE
                    this.replaceType = ReplaceType.NONE
                }
            }
        )
    }
}

@Composable
fun rememberFindAndReplaceState(): FindAndReplaceState {
    return rememberSaveable(saver = FindAndReplaceState.Saver) {
        FindAndReplaceState()
    }
}

enum class ScrollDirection {
    NEXT, PREVIOUS, NONE
}

enum class ReplaceType {
    ALL, CURRENT, NONE
}

@Composable
fun FindAndReplaceField(
    state: FindAndReplaceState,
    modifier: Modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, bottom = 4.dp)
) = if (isScreenWidthExpanded())
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FindField(modifier = Modifier.weight(1f), state = state)
        ReplaceField(modifier = Modifier.weight(1f), state = state)
    }
else
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FindField(modifier = Modifier.fillMaxWidth(), state = state)
        ReplaceField(modifier = Modifier.fillMaxWidth(), state = state)
    }

@Composable
private fun FindField(
    modifier: Modifier = Modifier,
    state: FindAndReplaceState
) = Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    val focusRequester = remember { FocusRequester() }
    CustomTextField(
        modifier = Modifier.padding(end = 4.dp).weight(1f).focusRequester(focusRequester),
        value = state.searchWord,
        onValueChange = { state.searchWord = it },
        leadingIcon = Icons.Outlined.LocationSearching,
        suffix = {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = if (state.searchWord.isBlank()) "" else state.position,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        },
        placeholderText = stringResource(Res.string.find)
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    IconButton(onClick = { state.scrollDirection = ScrollDirection.PREVIOUS }) {
        Icon(
            imageVector = Icons.Default.ArrowUpward, contentDescription = "PREVIOUS"
        )
    }
    IconButton(onClick = { state.scrollDirection = ScrollDirection.NEXT }) {
        Icon(
            imageVector = Icons.Default.ArrowDownward, contentDescription = "Next"
        )
    }
}

@Composable
private fun ReplaceField(
    modifier: Modifier = Modifier,
    state: FindAndReplaceState
) = Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    CustomTextField(
        modifier = Modifier.padding(end = 4.dp).weight(1f),
        value = state.replaceWord,
        onValueChange = { state.replaceWord = it },
        leadingIcon = Icons.Outlined.Autorenew,
        placeholderText = stringResource(Res.string.replace)
    )
    IconButton(onClick = { state.replaceType = ReplaceType.CURRENT }) {
        Icon(
            painter = painterResource(Res.drawable.replace),
            contentDescription = "Replace"
        )
    }
    IconButton(onClick = { state.replaceType = ReplaceType.ALL }) {
        Icon(
            painter = painterResource(Res.drawable.replace_all),
            contentDescription = "Replace all"
        )
    }
}

// 由于OutlinedTextField有诡异的边距和大小，因此自定义BasicTextField来实现
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholderText: String,
    modifier: Modifier = Modifier,
    suffix: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    focusManager: FocusManager = LocalFocusManager.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = BasicTextField(
    modifier = modifier
        .fillMaxWidth()
        .height(48.dp),
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    interactionSource = interactionSource,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    decorationBox = { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            interactionSource = interactionSource,
            visualTransformation = VisualTransformation.None,
            innerTextField = innerTextField,
            singleLine = true,
            enabled = true,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null
                )
            },
            placeholder = {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            trailingIcon = {
                if (value.isNotBlank()) {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Default),
                        shape = IconButtonDefaults.smallSquareShape,
                        onClick = { onValueChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Clear"
                        )
                    }
                }
            },
            suffix = suffix,
            contentPadding = PaddingValues(0.dp),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = true,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = OutlinedTextFieldDefaults.colors()
                        .copy(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                            errorTextColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                    shape = MaterialTheme.shapes.small
                )
            }
        )
    }
)
