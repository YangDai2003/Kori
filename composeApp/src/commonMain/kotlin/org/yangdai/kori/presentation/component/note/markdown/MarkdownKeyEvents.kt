package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.isPlatformActionKeyPressed
import org.yangdai.kori.presentation.component.note.addAfter
import org.yangdai.kori.presentation.component.note.bold
import org.yangdai.kori.presentation.component.note.bulletList
import org.yangdai.kori.presentation.component.note.code
import org.yangdai.kori.presentation.component.note.header
import org.yangdai.kori.presentation.component.note.highlight
import org.yangdai.kori.presentation.component.note.horizontalRule
import org.yangdai.kori.presentation.component.note.italic
import org.yangdai.kori.presentation.component.note.link
import org.yangdai.kori.presentation.component.note.math
import org.yangdai.kori.presentation.component.note.mermaidDiagram
import org.yangdai.kori.presentation.component.note.moveCursorLeftStateless
import org.yangdai.kori.presentation.component.note.moveCursorRightStateless
import org.yangdai.kori.presentation.component.note.numberedList
import org.yangdai.kori.presentation.component.note.quote
import org.yangdai.kori.presentation.component.note.strikeThrough
import org.yangdai.kori.presentation.component.note.taskList
import org.yangdai.kori.presentation.component.note.underline

fun Modifier.markdownKeyEvents(textFieldState: TextFieldState): Modifier =
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            if (keyEvent.isPlatformActionKeyPressed) {
                if (keyEvent.isShiftPressed) {
                    when (keyEvent.key) {

                        Key.B -> {
                            textFieldState.edit { bulletList() }
                            true
                        }

                        Key.N -> {
                            textFieldState.edit { numberedList() }
                            true
                        }

                        Key.T -> {
                            textFieldState.edit { taskList() }
                            true
                        }

                        Key.D -> {
                            textFieldState.edit { mermaidDiagram() }
                            true
                        }

                        else -> false
                    }
                } else {
                    when (keyEvent.key) {
                        Key.B -> {
                            textFieldState.edit { bold() }
                            true
                        }

                        Key.I -> {
                            textFieldState.edit { italic() }
                            true
                        }

                        Key.U -> {
                            textFieldState.edit { underline() }
                            true
                        }

                        Key.D -> {
                            textFieldState.edit { strikeThrough() }
                            true
                        }

                        Key.H -> {
                            textFieldState.edit { highlight() }
                            true
                        }

                        Key.E -> {
                            textFieldState.edit { code() }
                            true
                        }

                        Key.Q -> {
                            textFieldState.edit { quote() }
                            true
                        }

                        Key.R -> {
                            textFieldState.edit { horizontalRule() }
                            true
                        }

                        Key.L -> {
                            textFieldState.edit { link() }
                            true
                        }

                        Key.M -> {
                            textFieldState.edit { math() }
                            true
                        }

                        Key.NumPad1, Key.One -> {
                            textFieldState.edit { header(1) }
                            true
                        }

                        Key.NumPad2, Key.Two -> {
                            textFieldState.edit { header(2) }
                            true
                        }

                        Key.NumPad3, Key.Three -> {
                            textFieldState.edit { header(3) }
                            true
                        }

                        Key.NumPad4, Key.Four -> {
                            textFieldState.edit { header(4) }
                            true
                        }

                        Key.NumPad5, Key.Five -> {
                            textFieldState.edit { header(5) }
                            true
                        }

                        Key.NumPad6, Key.Six -> {
                            textFieldState.edit { header(6) }
                            true
                        }

                        else -> false
                    }
                }

            } else {
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

                    Key.Enter, Key.NumPadEnter -> { // 改进换行键行为
                        val currentText = textFieldState.text.toString()
                        val selection = textFieldState.selection

                        // 安全地获取当前行的内容
                        val currentLineStart = currentText.lastIndexOf(
                            '\n',
                            (selection.start - 1).coerceIn(0, currentText.length)
                        ).let {
                            if (it == -1) 0 else it + 1
                        }
                        val currentLineEnd =
                            currentText.indexOf('\n', selection.start).let {
                                if (it == -1) currentText.length else it
                            }

                        // 确保起始位置小于结束位置
                        if (currentLineStart >= currentLineEnd) {
                            textFieldState.edit {
                                addAfter("\n")
                            }
                            return@onPreviewKeyEvent true
                        }

                        val currentLine =
                            currentText.substring(currentLineStart, currentLineEnd)
                        val trimmedLine = currentLine.trim()

                        // 获取行首的缩进
                        val indentation =
                            currentLine.takeWhile { it.isWhitespace() }

                        // 处理空列表项
                        if (selection.start == currentLineEnd &&
                            (trimmedLine == "- [ ]" || trimmedLine == "-" || trimmedLine == "*" || trimmedLine == "+"
                                    || trimmedLine.matches(Regex("^\\d+\\.$")) || trimmedLine.matches(
                                Regex("^\\d+\\)$")
                            ))
                        ) {
                            textFieldState.edit {
                                delete(currentLineStart, currentLineEnd)
                            }
                            return@onPreviewKeyEvent true
                        }

                        val newLinePrefix = when {
                            trimmedLine.startsWith("- [ ] ") || trimmedLine.startsWith(
                                "- [x] "
                            ) -> "- [ ] " // 任务列表
                            trimmedLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                                val nextNumber =
                                    trimmedLine.substringBefore(".").toIntOrNull()
                                        ?.plus(1)
                                        ?: 1
                                "$nextNumber. " // 有序列表
                            }

                            trimmedLine.matches(Regex("^\\d+\\)\\s.*")) -> {
                                val nextNumber =
                                    trimmedLine.substringBefore(")").toIntOrNull()
                                        ?.plus(1)
                                        ?: 1
                                "$nextNumber) " // 有序列表
                            }

                            trimmedLine.startsWith("- ") -> "- " // 无序列表
                            trimmedLine.startsWith("* ") -> "* "
                            trimmedLine.startsWith("+ ") -> "+ "
                            else -> ""
                        }

                        textFieldState.edit {
                            addAfter("\n")
                            if (newLinePrefix.isNotEmpty()) {
                                addAfter(indentation + newLinePrefix)
                            }
                        }
                        true
                    }

                    else -> false
                }
            }
        } else {
            false
        }
    }