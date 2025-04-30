package kmark.parser.markerblocks.providers

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.AtxHeaderMarkerBlock

class AtxHeaderProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        val headerRange = matches(pos)
        return if (headerRange != null) {
            listOf(
                AtxHeaderMarkerBlock(
                    stateInfo.currentConstraints,
                    productionHolder,
                    headerRange,
                    calcTailStartPos(pos, headerRange.last),
                    pos.nextLineOrEofOffset
                )
            )
        } else {
            emptyList()
        }

    }

    private fun calcTailStartPos(pos: LookaheadText.Position, headerSize: Int): Int {
        val line = pos.currentLineFromPosition
        var offset = line.length - 1
        while (offset > headerSize && line[offset].isWhitespace()) {
            offset--
        }
        while (offset > headerSize && line[offset] == '#' && line[offset - 1] != '\\') {
            offset--
        }
        return if (offset + 1 < line.length && line[offset].isWhitespace() && line[offset + 1] == '#') {
            pos.offset + offset + 1
        } else {
            pos.offset + line.length
        }
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        return matches(pos) != null
    }

    private fun matches(pos: LookaheadText.Position): IntRange? {
        if (pos.offsetInCurrentLine != -1) {
            val text = pos.currentLineFromPosition
            var offset = MarkerBlockProvider.passSmallIndent(text)
            if (offset >= text.length || text[offset] != '#') {
                return null
            }

            val start = offset
            repeat(6) {
                if (offset < text.length && text[offset] == '#') {
                    offset++
                }
            }

            if (offset < text.length && text[offset] !in listOf(' ', '\t')) {
                return null
            }
            return IntRange(start, offset - 1)
        }
        return null
    }
}