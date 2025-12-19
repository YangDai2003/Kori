@file:OptIn(ExperimentalMaterial3Api::class)

package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.InputFieldHeight
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.SearchBarDefaults.inputFieldShape
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun TopSearchBar(
    searchBarState: SearchBarState,
    inputField: @Composable () -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit
) = Box(
    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
) {
    val colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    SearchBar(
        modifier = Modifier
            .windowInsetsPadding(SearchBarDefaults.windowInsets.only(WindowInsetsSides.Top)),
        state = searchBarState,
        inputField = inputField,
        colors = colors
    )
    val shadowElevation by animateDpAsState(targetValue = if (searchBarState.targetValue == SearchBarValue.Expanded) 4.dp else 0.dp)
    ExpandedSearchBar(
        state = searchBarState,
        inputField = inputField,
        colors = colors,
        shadowElevation = shadowElevation,
        content = expandedContent
    )
}

private val SearchBarState.collapsedBounds: IntRect
    get() = collapsedCoords?.let { IntRect(offset = it.positionInParent().round(), size = it.size) }
        ?: IntRect.Zero

private val SearchBarState.isExpanded
    get() = this.currentValue == SearchBarValue.Expanded

@Composable
private fun ExpandedSearchBar(
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    shape: Shape = SearchBarDefaults.dockedShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
    properties: PopupProperties = PopupProperties(focusable = true, clippingEnabled = false),
    content: @Composable ColumnScope.() -> Unit
) = ExpandedSearchBarImpl(state = state, properties = properties) { focusRequester ->
    SearchBarLayout(
        state = state,
        inputField = {
            Box(
                modifier = Modifier.focusRequester(focusRequester),
                propagateMinConstraints = true,
            ) {
                inputField()
            }
        },
        shape = shape,
        colors = colors,
        shadowElevation = shadowElevation,
        content = content
    )
}

@Composable
private fun ExpandedSearchBarImpl(
    state: SearchBarState,
    properties: PopupProperties,
    content: @Composable (FocusRequester) -> Unit
) {
    if (!state.isExpanded) return

    val scope = rememberCoroutineScope()

    Popup(
        offset = state.collapsedBounds.topLeft,
        onDismissRequest = { scope.launch { state.animateToCollapsed() } },
        properties = properties
    ) {
        val focusRequester = remember { FocusRequester() }
        content(focusRequester)

        // Focus the input field on the first expansion,
        // but no need to re-focus if the focus gets cleared.
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        // Manually dismiss keyboard when search bar is collapsed.
        // Otherwise, the search bar's window closes and the keyboard disappears suddenly.
        val softwareKeyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(state.targetValue) {
            if (state.targetValue == SearchBarValue.Collapsed) {
                softwareKeyboardController?.hide()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchBarLayout(
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    shape: Shape,
    colors: SearchBarColors,
    shadowElevation: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    BackHandler(enabled = state.isExpanded) { scope.launch { state.animateToCollapsed() } }

    Surface(
        shape = shape,
        color = colors.containerColor,
        contentColor = contentColorFor(colors.containerColor),
        shadowElevation = shadowElevation
    ) {
        val windowContainerHeight = LocalWindowInfo.current.containerDpSize.height
        val maxHeight = windowContainerHeight * 2f / 3f
        val minHeight = 240.dp.coerceAtMost(maxHeight)

        Layout(
            contents =
                listOf(
                    inputField,
                    {
                        Column {
                            HorizontalDivider(color = colors.dividerColor)
                            content()
                        }
                    },
                )
        ) { measurables, baseConstraints ->
            val (inputFieldMeasurables, contentMeasurables) = measurables
            val constraintMaxHeight =
                lerp(state.collapsedBounds.height, maxHeight.roundToPx(), state.progress)
            val constraints =
                baseConstraints.constrain(
                    Constraints(
                        minHeight = minHeight.roundToPx().coerceAtMost(constraintMaxHeight),
                        maxHeight = constraintMaxHeight,
                    )
                )
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            val inputFieldPlaceables =
                inputFieldMeasurables.fastMap { it.measure(looseConstraints) }
            val inputFieldWidth = inputFieldPlaceables.fastMaxOfOrNull { it.width } ?: 0
            val inputFieldHeight = inputFieldPlaceables.fastMaxOfOrNull { it.height } ?: 0

            val contentConstraints =
                looseConstraints
                    .offset(vertical = -inputFieldHeight)
                    .copy(maxWidth = inputFieldWidth)
            val contentPlaceables = contentMeasurables.fastMap { it.measure(contentConstraints) }

            val height = inputFieldHeight + (contentPlaceables.fastMaxOfOrNull { it.height } ?: 0)
            val width = max(inputFieldWidth, contentPlaceables.fastMaxOfOrNull { it.width } ?: 0)

            layout(constraints.constrainWidth(width), constraints.constrainHeight(height)) {
                inputFieldPlaceables.fastForEach { it.place(0, 0) }
                contentPlaceables.fastForEach { it.place(0, inputFieldHeight) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchBarInputField(
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    onSearch: (String) -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = inputFieldShape,
    colors: TextFieldColors = inputFieldColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    /*
    Relationship between focus and expansion state:
        * In touch mode, the two are coupled:
            * Text field gains focus => search bar expands
            * Search bar collapses => text field loses focus
        * In non-touch/keyboard mode, they are independent. Instead, expansion triggers when:
            * the user starts typing
            * the user presses the down direction key
     */
    val focused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val isInTouchMode = LocalInputModeManager.current.inputMode == InputMode.Touch

    val textColor = textStyle.color.takeOrElse {
        colors.textColor(true, isError = false, focused = focused)
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    val coroutineScope = rememberCoroutineScope()

    BasicTextField(
        state = textFieldState,
        modifier = Modifier
            .onPreviewKeyEvent {
                val expandOnDownKey = !isInTouchMode && !searchBarState.isExpanded
                if (expandOnDownKey && it.key == Key.DirectionDown) {
                    coroutineScope.launch { searchBarState.animateToExpanded() }
                    return@onPreviewKeyEvent true
                }
                // Make sure arrow key down moves to list of suggestions.
                if (searchBarState.isExpanded && it.key == Key.DirectionDown) {
                    focusManager.moveFocus(FocusDirection.Down)
                    return@onPreviewKeyEvent true
                }
                false
            }
            .width(320.dp)
            .sizeIn(minHeight = InputFieldHeight)
            .onFocusChanged {
                if (it.isFocused && isInTouchMode) {
                    coroutineScope.launch { searchBarState.animateToExpanded() }
                }
            },
        lineLimits = lineLimits,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError = false)),
        keyboardOptions = keyboardOptions,
        onKeyboardAction = { onSearch(textFieldState.text.toString()) },
        interactionSource = interactionSource,
        scrollState = scrollState,
        decorator =
            TextFieldDefaults.decorator(
                state = textFieldState,
                enabled = true,
                lineLimits = lineLimits,
                outputTransformation = null,
                interactionSource = interactionSource,
                placeholder = placeholder,
                leadingIcon = leadingIcon?.let { leading ->
                    { Box(Modifier.offset(x = 4.dp)) { leading() } }
                },
                trailingIcon = trailingIcon?.let { trailing ->
                    { Box(Modifier.offset(x = (-4).dp)) { trailing() } }
                },
                prefix = prefix,
                suffix = suffix,
                colors = colors,
                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                container = {
                    val containerColor = animateColorAsState(
                        targetValue = colors.containerColor(
                            enabled = true,
                            isError = false,
                            focused = focused,
                        ),
                        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                    )
                    Box(Modifier.drawWithCache {
                        val outline = shape.createOutline(size, layoutDirection, this)
                        onDrawBehind { drawOutline(outline, color = containerColor.value) }
                    })
                },
            ),
    )

    // Most expansions from touch happen via `onFocusChanged` above, but in a mixed
    // keyboard-touch flow, the user can focus via keyboard (with no expansion),
    // and subsequent touches won't change focus state. So this effect is needed as well.
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                if (!searchBarState.isExpanded) {
                    coroutineScope.launch { searchBarState.animateToExpanded() }
                }
            }
        }
    }

    // Expand search bar if the user starts typing
    LaunchedEffect(searchBarState, textFieldState) {
        if (!searchBarState.isExpanded) {
            var prevLength = textFieldState.text.length
            snapshotFlow { textFieldState.text }
                .onEach {
                    val currLength = it.length
                    if (currLength > prevLength && focused && !searchBarState.isExpanded) {
                        // Don't use LaunchedEffect's coroutine because
                        // cancelling the animation shouldn't cancel the Flow
                        coroutineScope.launch { searchBarState.animateToExpanded() }
                    }
                    prevLength = currLength
                }
                .collect {}
        }
    }

    LaunchedEffect(searchBarState.isExpanded) {
        val shouldClearFocusOnCollapse = !searchBarState.isExpanded && focused && isInTouchMode
        if (shouldClearFocusOnCollapse) focusManager.clearFocus()
    }
}