package kmark.parser.markerblocks

import kmark.IElementType
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints


abstract class MarkerBlockImpl(
    protected val constraints: MarkdownConstraints,
    protected val marker: ProductionHolder.Marker
) : MarkerBlock {

    private var lastInterestingOffset: Int = -2

    private var scheduledResult: MarkerBlock.ProcessingResult? = null

    final override fun getNextInterestingOffset(pos: LookaheadText.Position): Int {
        if (scheduledResult != null) {
            return pos.offset + 1
        }

        if (lastInterestingOffset != -1 && lastInterestingOffset <= pos.offset) {
            lastInterestingOffset = calcNextInterestingOffset(pos)
        }
        return lastInterestingOffset
    }

    final override fun processToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints
    ): MarkerBlock.ProcessingResult {
        if (lastInterestingOffset != pos.offset && scheduledResult != null) {
            return MarkerBlock.ProcessingResult.CANCEL
        }

        if (lastInterestingOffset == -1 || lastInterestingOffset > pos.offset) {
            return MarkerBlock.ProcessingResult.PASS
        }
        if (lastInterestingOffset < pos.offset && !isInterestingOffset(pos)) {
            return MarkerBlock.ProcessingResult.PASS
        }
        if (scheduledResult != null) {
            return scheduledResult!!
        }
        return doProcessToken(pos, currentConstraints)
    }

    final override fun getBlockConstraints(): MarkdownConstraints {
        return constraints
    }

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        var actionToRun = action
        if (actionToRun == MarkerBlock.ClosingAction.DEFAULT) {
            actionToRun = getDefaultAction()
        }

        actionToRun.doAction(marker, getDefaultNodeType())

        return actionToRun != MarkerBlock.ClosingAction.NOTHING

    }

    protected fun scheduleProcessingResult(offset: Int, result: MarkerBlock.ProcessingResult) {
        lastInterestingOffset = offset
        scheduledResult = result
    }

    protected abstract fun getDefaultAction(): MarkerBlock.ClosingAction

    protected abstract fun doProcessToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints
    ): MarkerBlock.ProcessingResult

    protected abstract fun calcNextInterestingOffset(pos: LookaheadText.Position): Int

    abstract fun getDefaultNodeType(): IElementType
}
