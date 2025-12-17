package kmark.parser.markerblocks.providers

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.HorizontalRuleMarkerBlock

class HorizontalRuleProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        return if (matches(pos, stateInfo.currentConstraints)) {
            listOf(HorizontalRuleMarkerBlock(stateInfo.currentConstraints, productionHolder.mark()))
        } else {
            emptyList()
        }
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        return matches(pos, constraints)
    }

    fun matches(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return false
        }
        return isHorizontalRule(pos.currentLine, pos.offsetInCurrentLine)
    }

    companion object {
        fun isHorizontalRule(line: CharSequence, offset: Int): Boolean {
            var hrChar: Char? = null
            var startSpace = 0
            var charCount = 1
            for (i in offset..<line.length) {
                val c = line[i]
                if (hrChar == null) {
                    if (c == '*' || c == '-' || c == '_') {
                        hrChar = c
                    } else if (startSpace < 3 && c == ' ') {
                        startSpace++
                    } else {
                        return false
                    }
                } else {
                    if (c == hrChar) {
                        charCount++
                    } else if (c != ' ' && c != '\t') {
                        return false
                    }
                }
            }
            return charCount >= 3
        }

    }
}