package kmark.parser.markerblocks.providers

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.getCharsEaten
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.BlockQuoteMarkerBlock

class BlockQuoteProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
//        if (Character.isWhitespace(pos.char)) {
//            return emptyList()
//        }

        val currentConstraints = stateInfo.currentConstraints
        val nextConstraints = stateInfo.nextConstraints
        if (pos.offsetInCurrentLine != currentConstraints.getCharsEaten(pos.currentLine)) {
            return emptyList()
        }
        return if (nextConstraints != currentConstraints && nextConstraints.types.lastOrNull() == '>') {
            listOf(BlockQuoteMarkerBlock(nextConstraints, productionHolder.mark()))
        } else {
            emptyList()
        }
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        // Actually, blockquote may interrupt a paragraph, but we have MarkdownConstraints for these cases
        return false
    }
}