package kmark.parser.markerblocks

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.getCharsEaten

interface MarkerBlockProvider<T : MarkerProcessor.StateInfo> {
    fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: T
    ): List<MarkerBlock>

    fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean

    companion object {
        fun isStartOfLineWithConstraints(
            pos: LookaheadText.Position,
            constraints: MarkdownConstraints
        ): Boolean {
            return pos.offsetInCurrentLine == constraints.getCharsEaten(pos.currentLine)
        }

        fun passSmallIndent(text: CharSequence, startOffset: Int = 0): Int {
            var offset = startOffset
            repeat(3) {
                if (offset < text.length && text[offset] == ' ') {
                    offset++
                }
            }
            return offset
        }
    }
}