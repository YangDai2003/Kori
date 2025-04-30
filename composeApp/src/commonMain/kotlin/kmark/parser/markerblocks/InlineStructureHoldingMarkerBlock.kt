package kmark.parser.markerblocks

import kmark.MarkdownElementTypes
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.sequentialparsers.SequentialParser


abstract class InlineStructureHoldingMarkerBlock(
    constraints: MarkdownConstraints,
    protected val productionHolder: ProductionHolder
) : MarkerBlockImpl(constraints, productionHolder.mark()) {

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        if (action != MarkerBlock.ClosingAction.NOTHING) {
            if (action == MarkerBlock.ClosingAction.DONE
                || action == MarkerBlock.ClosingAction.DEFAULT
                && getDefaultAction() == MarkerBlock.ClosingAction.DONE
            ) {
                for (range in getRangesContainingInlineStructure()) {
                    productionHolder.addProduction(
                        listOf(SequentialParser.Node(range, MarkdownElementTypes.ATX_1))
                    )
                }
            }
        }

        return super.acceptAction(action)
    }

    abstract fun getRangesContainingInlineStructure(): Collection<IntRange>
}
