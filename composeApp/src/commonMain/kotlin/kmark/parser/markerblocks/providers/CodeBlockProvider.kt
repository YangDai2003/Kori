package kmark.parser.markerblocks.providers

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.getCharsEaten
import kmark.parser.markerblocks.MarkdownParserUtil
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.CodeBlockMarkerBlock

class CodeBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        if (stateInfo.nextConstraints.getCharsEaten(pos.currentLine) > pos.offsetInCurrentLine) {
            return emptyList()
        }

        val charsToNonWhitespace = pos.charsToNonWhitespace()
            ?: return emptyList()
        val blockStart = pos.nextPosition(charsToNonWhitespace)
            ?: return emptyList()

        return if (MarkdownParserUtil.hasCodeBlockIndent(
                blockStart,
                stateInfo.currentConstraints
            )
        ) {
            listOf(CodeBlockMarkerBlock(stateInfo.currentConstraints, productionHolder, pos))
        } else {
            emptyList()
        }
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        return false
    }

}