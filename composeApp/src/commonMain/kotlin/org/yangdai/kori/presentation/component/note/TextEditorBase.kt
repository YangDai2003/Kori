package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextLayoutResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TextEditorBase(
    state: TextFieldState,
    scrollState: ScrollState,
    readMode: Boolean,
    showLineNumbers: Boolean,
    headerRange: IntRange?,
    textLayoutResult: TextLayoutResult?,
    findAndReplaceState: FindAndReplaceState,
    onFindAndReplaceUpdate: (FindAndReplaceState) -> Unit,
    content: @Composable (
        indices: List<Pair<Int, Int>>,
        currentIndex: Int,
        actualLinePositions: List<Pair<Int, Float>>
    ) -> Unit
) {
    // Shared state management
    var indices by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var actualLinePositions by remember { mutableStateOf<List<Pair<Int, Float>>>(emptyList()) }

    // Search functionality
    LaunchedEffect(state.text, findAndReplaceState.searchWord, readMode) {
        indices = if (!readMode && findAndReplaceState.searchWord.isNotBlank()) {
            findAllIndices(state.text.toString(), findAndReplaceState.searchWord)
        } else {
            emptyList()
        }
    }

    // Handle search navigation
    LaunchedEffect(findAndReplaceState.searchWord, indices, findAndReplaceState.scrollDirection) {
        if (indices.isNotEmpty() && textLayoutResult != null && findAndReplaceState.scrollDirection != null) {
            // Update current index
            currentIndex = when (findAndReplaceState.scrollDirection) {
                ScrollDirection.NEXT -> (currentIndex + 1).takeIf { it < indices.size }
                    ?: 0 // Wrap around
                ScrollDirection.PREVIOUS -> (currentIndex - 1).takeIf { it >= 0 }
                    ?: (indices.size - 1) // Wrap around
            }

            // Get the target match position
            val targetMatch = indices[currentIndex]
            // Get the text bounds
            val bounds = textLayoutResult.getBoundingBox(targetMatch.first)
            // Calculate scroll position
            val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
            // Execute scroll
            scrollState.animateScrollTo(scrollPosition)
            // Notify scroll completion
            onFindAndReplaceUpdate(findAndReplaceState.copy(scrollDirection = null))
        }
    }

    // Handle header navigation
    LaunchedEffect(headerRange) {
        headerRange?.let {
            textLayoutResult?.let { layout ->
                // Get bounds of that position
                val bounds = layout.getBoundingBox(headerRange.first)
                // Calculate scroll position
                val scrollPosition = (bounds.top - 50f).toInt().coerceAtLeast(0)
                // Execute scroll
                scrollState.animateScrollTo(scrollPosition)
            }
        }
    }

    // Handle replace functionality
    LaunchedEffect(findAndReplaceState.replaceType) {
        if (findAndReplaceState.replaceType != null && findAndReplaceState.searchWord.isNotBlank()) {
            if (findAndReplaceState.replaceType == ReplaceType.ALL) {
                val currentText = state.text.toString()
                val newText = currentText.replace(
                    findAndReplaceState.searchWord,
                    findAndReplaceState.replaceWord
                )
                state.setTextAndPlaceCursorAtEnd(newText)
            } else if (findAndReplaceState.replaceType == ReplaceType.CURRENT) {
                // Check if index is valid
                if (indices.isEmpty() || currentIndex >= indices.size) return@LaunchedEffect
                // Get the position to be replaced
                val (startIndex, endIndex) = indices[currentIndex]
                // Execute replacement
                state.edit {
                    replace(startIndex, endIndex, findAndReplaceState.replaceWord)
                }
            }
            onFindAndReplaceUpdate(findAndReplaceState.copy(replaceType = null))
        }
    }

    LaunchedEffect(showLineNumbers, textLayoutResult) {
        // Exit condition: No line numbers needed or layout result isn't ready
        if (!showLineNumbers || textLayoutResult == null) return@LaunchedEffect

        val currentText = state.text

        withContext(Dispatchers.Default) {
            val lineCount = textLayoutResult.lineCount
            val newLines = ArrayList<Pair<Int, Float>>()

            for (lineIndex in 0 until lineCount) {
                // Get the starting character offset of the line
                val lineStartOffset = textLayoutResult.getLineStart(lineIndex)

                // Determine if this line should be included based on the requirement
                val includeLine = if (lineIndex == 0) {
                    // Always include the very first line
                    true
                } else {
                    // For subsequent lines, check if the previous character exists and is a newline
                    // Check lineStartOffset > 0 to safely access index [lineStartOffset - 1]
                    // Add <= currentText.length check for robustness against potential edge cases in textLayoutResult
                    lineStartOffset > 0 && lineStartOffset <= currentText.length && currentText[lineStartOffset - 1] == '\n'
                }

                // If the line meets the criteria, get its top position and add it
                if (includeLine) {
                    val lineTopY = textLayoutResult.getLineTop(lineIndex)
                    newLines.add(lineStartOffset to lineTopY)
                }
            }

            if (newLines != actualLinePositions) {
                actualLinePositions = newLines
            }
        }
    }

    content(indices, currentIndex, actualLinePositions)
}

private suspend fun findAllIndices(text: String, word: String): List<Pair<Int, Int>> {
    if (word.isBlank()) return emptyList()

    return withContext(Dispatchers.Default) {
        buildList {
            var index = text.indexOf(word)
            while (index != -1) {
                add(index to (index + word.length))
                index = text.indexOf(word, index + 1)
            }
        }
    }
}