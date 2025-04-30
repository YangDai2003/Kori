package kmark.parser.markerblocks.providers

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.eatItselfFromString
import kmark.parser.constraints.extendsPrev
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.SetextHeaderMarkerBlock

class SetextHeaderProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        if (stateInfo.paragraphBlock != null) {
            return emptyList()
        }
        val currentConstaints = stateInfo.currentConstraints
        if (stateInfo.nextConstraints != currentConstaints) {
            return emptyList()
        }
        return if (MarkerBlockProvider.isStartOfLineWithConstraints(pos, currentConstaints)
            && getNextLineFromConstraints(pos, currentConstaints)?.let { REGEX.matches(it) } == true
        ) {
            listOf(SetextHeaderMarkerBlock(currentConstaints, productionHolder))
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

    private fun getNextLineFromConstraints(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): CharSequence? {
        val line = pos.nextLine ?: return null
        val nextLineConstraints = constraints.applyToNextLine(pos.nextLinePosition())
        return if (nextLineConstraints.extendsPrev(constraints)) {
            nextLineConstraints.eatItselfFromString(line)
        } else {
            null
        }
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(-+|=+) *$")
    }
}
