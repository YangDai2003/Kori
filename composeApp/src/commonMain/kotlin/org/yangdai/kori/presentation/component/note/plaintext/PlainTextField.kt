package org.yangdai.kori.presentation.component.note.plaintext

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.content
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.FindAndReplaceState
import org.yangdai.kori.presentation.component.note.LineNumbersColumn
import org.yangdai.kori.presentation.component.note.TextEditorBase

@Composable
fun PlainTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit
){
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    TextEditorBase(
        state = state,
        scrollState = scrollState,
        readMode = readMode,
        showLineNumbers = showLineNumbers,
        headerRange = null,
        findAndReplaceState = findAndReplaceState,
        onFindAndReplaceUpdate = onFindAndReplaceUpdate,
        textLayoutResult = textLayoutResult
    ) { indices, currentIndex, actualLinePositions ->
        val currentLine by remember(state.selection, actualLinePositions) {
            derivedStateOf {
                actualLinePositions.indexOfLast { (startIndex, _) ->
                    startIndex <= state.selection.start
                }.coerceAtLeast(0)
            }
        }
        Row(modifier) {
            if (showLineNumbers) {
                LineNumbersColumn(
                    currentLine = currentLine,
                    actualLinePositions = actualLinePositions,
                    scrollProvider = { scrollState.value }
                )
                VerticalDivider()
            }

            BasicTextField(
                modifier = Modifier
                    .padding(start = if (showLineNumbers) 4.dp else 16.dp, end = 16.dp)
                    .fillMaxSize(),
                scrollState = scrollState,
                readOnly = readMode,
                state = state,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { result -> textLayoutResult = result.invoke() },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .clipToBounds()
                            .drawBehind {
                                textLayoutResult?.let { layoutResult ->
                                    // 提前计算滚动偏移,避免重复计算
                                    val scrollOffset = Offset(0f, -scrollState.value.toFloat())
                                    val text = state.text.toString()

                                    // 批量绘制搜索高亮
                                    if (findAndReplaceState.searchWord.isNotBlank()) {
                                        // 使用 withTransform 避免多次 translate
                                        withTransform({
                                            translate(
                                                scrollOffset.x,
                                                scrollOffset.y
                                            )
                                        }) {
                                            indices.forEachIndexed { index, (start, end) ->
                                                if (start < end && end <= text.length) {
                                                    val path =
                                                        layoutResult.getPathForRange(start, end)
                                                    drawPath(
                                                        path = path,
                                                        color = if (index == currentIndex) Color.Green else Color.Cyan,
                                                        alpha = 0.5f,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.content),
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}