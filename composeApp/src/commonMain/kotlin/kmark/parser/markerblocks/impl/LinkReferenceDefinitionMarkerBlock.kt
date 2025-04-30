package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl

class LinkReferenceDefinitionMarkerBlock(
    myConstraints: MarkdownConstraints,
    marker: ProductionHolder.Marker,
    private val endPosition: Int
) : MarkerBlockImpl(myConstraints, marker) {
    override fun allowsSubBlocks(): Boolean = false

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints
    ): MarkerBlock.ProcessingResult {
        if (pos.offset < endPosition) {
            return MarkerBlock.ProcessingResult.CANCEL
        }
        return MarkerBlock.ProcessingResult.DEFAULT
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return endPosition
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.LINK_DEFINITION
    }

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean {
        return true
    }

}