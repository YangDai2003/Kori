package kmark.parser.markerblocks.providers

import kmark.lexer.Compat.assert
import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.HtmlBlockMarkerBlock

class HtmlBlockProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        val matchingGroup = matches(pos, stateInfo.currentConstraints)
        if (matchingGroup != -1) {
            return listOf(
                HtmlBlockMarkerBlock(
                    stateInfo.currentConstraints,
                    productionHolder,
                    OPEN_CLOSE_REGEXES[matchingGroup].second,
                    pos
                )
            )
        }
        return emptyList()
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        return matches(pos, constraints) in 0..5
    }

    private fun matches(pos: LookaheadText.Position, constraints: MarkdownConstraints): Int {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return -1
        }
        val text = pos.currentLineFromPosition
        val offset = MarkerBlockProvider.passSmallIndent(text)
        if (offset >= text.length || text[offset] != '<') {
            return -1
        }

        val matchResult = FIND_START_REGEX.find(text.substring(offset))
            ?: return -1
        assert(matchResult.groups.size == OPEN_CLOSE_REGEXES.size + 2) { "There are some excess capturing groups probably!" }
        for (i in 0..<OPEN_CLOSE_REGEXES.size) {
            if (matchResult.groups[i + 2] != null) {
                return i
            }
        }
        assert(false) { "Match found but all groups are empty!" }
        return -1
    }

    companion object {
        const val TAG_NAMES =
            "address, article, aside, base, basefont, blockquote, body, caption, center, col, colgroup, dd, details, " +
                    "dialog, dir, div, dl, dt, fieldset, figcaption, figure, footer, form, frame, frameset, h1, " +
                    "head, header, hr, html, legend, li, link, main, menu, menuitem, meta, nav, noframes, ol, " +
                    "optgroup, option, p, param, pre, section, source, title, summary, table, tbody, td, tfoot, " +
                    "th, thead, title, tr, track, ul"

        const val TAG_NAME = "[a-zA-Z][a-zA-Z0-9-]*"

        const val ATTR_NAME = "[A-Za-z:_][A-Za-z0-9_.:-]*"

        const val ATTR_VALUE = "\\s*=\\s*(?:[^ \"'=<>`]+|'[^']*'|\"[^\"]*\")"

        const val ATTRIBUTE = "\\s+${ATTR_NAME}(?:${ATTR_VALUE})?"

        const val OPEN_TAG = "<${TAG_NAME}(?:${ATTRIBUTE})*\\s*/?>"

        /**
         * Closing tag allowance is not in public spec version yet
         */
        const val CLOSE_TAG = "</${TAG_NAME}\\s*>"

        /** see {@link http://spec.commonmark.org/0.21/#html-blocks}
         *
         * nulls mean "Next line should be blank"
         * */
        val OPEN_CLOSE_REGEXES: List<Pair<Regex, Regex?>> = listOf(
            Pair(
                Regex("<(?:script|pre|style)(?: |>|$)", RegexOption.IGNORE_CASE),
                Regex("</(?:script|style|pre)>", RegexOption.IGNORE_CASE)
            ),
            Pair(Regex("<!--"), Regex("-->")),
            Pair(Regex("<\\?"), Regex("\\?>")),
            Pair(Regex("<![A-Z]"), Regex(">")),
            Pair(Regex("<!\\[CDATA\\["), Regex("]]>")),
            Pair(
                Regex(
                    "</?(?:${TAG_NAMES.replace(", ", "|")})(?: |/?>|$)",
                    RegexOption.IGNORE_CASE
                ), null
            ),
            Pair(Regex("(?:${OPEN_TAG}|${CLOSE_TAG})(?: |$)"), null)
        )

        val FIND_START_REGEX = Regex(
            "^(${
                OPEN_CLOSE_REGEXES.joinToString(
                    separator = "|",
                    transform = { "(${it.first.pattern})" })
            })"
        )

    }
}