package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl
import kmark.parser.sequentialparsers.SequentialParser

class AtxHeaderMarkerBlock(
    myConstraints: MarkdownConstraints,
    productionHolder: ProductionHolder,
    headerRange: IntRange,
    tailStartPos: Int,
    endOfLinePos: Int
) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun allowsSubBlocks(): Boolean = false

    init {
        val curPos = productionHolder.currentPosition
        val nodes = buildList {
            add(
                SequentialParser.Node(
                    curPos + headerRange.first..curPos + headerRange.last + 1,
                    MarkdownTokenTypes.ATX_HEADER
                )
            )
            if (curPos + headerRange.last + 1 != tailStartPos) {
                add(
                    SequentialParser.Node(
                        curPos + headerRange.last + 1..tailStartPos, MarkdownTokenTypes.ATX_CONTENT
                    )
                )
            }
            if (tailStartPos != endOfLinePos) {
                add(
                    SequentialParser.Node(
                        tailStartPos..endOfLinePos, MarkdownTokenTypes.ATX_HEADER
                    )
                )
            }
        }
        productionHolder.addProduction(nodes)
    }

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    private val nodeType = calcNodeType(headerRange.last - headerRange.first + 1)

    private fun calcNodeType(headerSize: Int): IElementType {
        return when (headerSize) {
            1 -> MarkdownElementTypes.ATX_1
            2 -> MarkdownElementTypes.ATX_2
            3 -> MarkdownElementTypes.ATX_3
            4 -> MarkdownElementTypes.ATX_4
            5 -> MarkdownElementTypes.ATX_5
            6 -> MarkdownElementTypes.ATX_6
            else -> MarkdownElementTypes.ATX_6
        }
    }

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun doProcessToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints
    ): MarkerBlock.ProcessingResult {
        if (pos.offsetInCurrentLine == -1) {
            return MarkerBlock.ProcessingResult(
                MarkerBlock.ClosingAction.DROP,
                MarkerBlock.ClosingAction.DONE,
                MarkerBlock.EventAction.PROPAGATE
            )
        }
        return MarkerBlock.ProcessingResult.CANCEL
    }

}