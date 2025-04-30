package kmark.parser.markerblocks.providers

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.ListItemMarkerBlock
import kmark.parser.markerblocks.impl.ListMarkerBlock

class ListMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {

//        if (Character.isWhitespace(pos.char)) {
//            return emptyList()
//        }
//        if (pos.offsetInCurrentLine != 0 && !Character.isWhitespace(pos.currentLine[pos.offsetInCurrentLine - 1])) {
//            return emptyList()
//        }

        val currentConstraints = stateInfo.currentConstraints
        val nextConstraints = stateInfo.nextConstraints

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, currentConstraints)) {
            return emptyList()
        }
        if (nextConstraints != currentConstraints
            && nextConstraints.types.lastOrNull() != '>' && nextConstraints.getLastExplicit() == true
        ) {

            val result = ArrayList<MarkerBlock>()
            if (stateInfo.lastBlock !is ListMarkerBlock) {
                result.add(
                    ListMarkerBlock(
                        nextConstraints,
                        productionHolder.mark(),
                        nextConstraints.types.lastOrNull()!!
                    )
                )
            }
            result.add(ListItemMarkerBlock(nextConstraints, productionHolder.mark()))
            return result
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        // Actually, list item interrupts a paragraph, but we have MarkdownConstraints for these cases
        return false
    }

    private fun MarkdownConstraints.getLastExplicit(): Boolean? {
        return isExplicit.lastOrNull()
    }
}