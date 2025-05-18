package kmark.parser.markerblocks

import kmark.lexer.Compat.assert
import kmark.parser.LookaheadText
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.applyToNextLineAndAddModifiers
import kmark.parser.constraints.eatItselfFromString
import kmark.parser.constraints.extendsPrev
import kmark.parser.constraints.getCharsEaten
import kmark.parser.constraints.upstreamWith

object MarkdownParserUtil {

    fun calcNumberOfConsequentEols(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Int {
        assert(pos.offsetInCurrentLine == -1)

        var currentPos = pos
        var result = 1

        val isClearLine: (LookaheadText.Position) -> Boolean = { pos ->
            val currentConstraints = constraints.applyToNextLine(pos)
            val constraintsLength = currentConstraints.getCharsEaten(pos.currentLine)

            currentConstraints.upstreamWith(constraints) && (
                    constraintsLength >= pos.currentLine.length ||
                            pos.nextPosition(1 + constraintsLength)?.charsToNonWhitespace() == null)
        }

        while (isClearLine(currentPos)) {
            currentPos = currentPos.nextLinePosition()
                ?: break//return 5

            result++
            if (result > 4) {
                break
            }
        }
        return result
    }

    fun getFirstNonWhitespaceLinePos(
        pos: LookaheadText.Position,
        eolsToSkip: Int
    ): LookaheadText.Position? {
        var currentPos = pos
        repeat(eolsToSkip - 1) {
            currentPos = pos.nextLinePosition() ?: return null
        }
        while (currentPos.charsToNonWhitespace() == null) {
            currentPos = currentPos.nextLinePosition()
                ?: return null
        }
        return currentPos
    }

    fun hasCodeBlockIndent(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        val constraintsLength = constraints.getCharsEaten(pos.currentLine)

        if (pos.offsetInCurrentLine >= constraintsLength + 4) {
            return true
        }
        for (i in constraintsLength..pos.offsetInCurrentLine) {
            if (pos.currentLine[i] == '\t') {
                return true
            }
        }
        return false
    }

    fun isEmptyOrSpaces(s: CharSequence): Boolean {
        for (c in s) {
            if (c != ' ' && c != '\t') {
                return false
            }
        }
        return true
    }

    fun findNonEmptyLineWithSameConstraints(
        constraints: MarkdownConstraints,
        pos: LookaheadText.Position
    ): LookaheadText.Position? {
        var currentPos = pos

        while (true) {
//            currentPos = currentPos.nextLinePosition() ?: return null

            val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(currentPos)
            // kinda equals
            if (!(nextLineConstraints.upstreamWith(constraints) && nextLineConstraints.extendsPrev(
                    constraints
                ))
            ) {
                return null
            }

            val stringAfterConstraints =
                nextLineConstraints.eatItselfFromString(currentPos.currentLine)

            if (!isEmptyOrSpaces(stringAfterConstraints)) {
                return currentPos
            } else {
                currentPos = currentPos.nextLinePosition()
                    ?: return null
            }
        }
    }

}
