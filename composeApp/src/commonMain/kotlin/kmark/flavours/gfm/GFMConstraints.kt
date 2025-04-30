package kmark.flavours.gfm

import kmark.parser.LookaheadText
import kmark.parser.constraints.CommonMarkdownConstraints

class GFMConstraints(
    indents: IntArray,
    types: CharArray,
    isExplicit: BooleanArray,
    charsEaten: Int,
    private val isCheckbox: Boolean
) : CommonMarkdownConstraints(indents, types, isExplicit, charsEaten) {
    override val base: CommonMarkdownConstraints
        get() = BASE

    override fun createNewConstraints(
        indents: IntArray,
        types: CharArray,
        isExplicit: BooleanArray,
        charsEaten: Int
    ): CommonMarkdownConstraints {
        val initialType = types[types.size - 1]
        val originalType = toOriginalType(initialType)
        types[types.size - 1] = originalType
        return GFMConstraints(indents, types, isExplicit, charsEaten, initialType != originalType)
    }

    fun hasCheckbox(): Boolean {
        return isCheckbox
    }

    override fun fetchListMarker(pos: LookaheadText.Position): ListMarkerInfo? {
        val baseMarkerInfo = super.fetchListMarker(pos)
            ?: return null

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine + baseMarkerInfo.markerLength

        while (offset < line.length && (line[offset] == ' ' || line[offset] == '\t')) {
            offset++
        }

        return if (offset + 3 <= line.length
            && line[offset] == '['
            && line[offset + 2] == ']'
            && (line[offset + 1] == 'x' || line[offset + 1] == 'X' || line[offset + 1] == ' ')
        ) {
            ListMarkerInfo(
                offset + 3 - pos.offsetInCurrentLine,
                toCheckboxType(baseMarkerInfo.markerType),
                baseMarkerInfo.markerLength
            )
        } else {
            baseMarkerInfo
        }
    }

    companion object {
        val BASE: GFMConstraints =
            GFMConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0, false)

        private fun toCheckboxType(originalType: Char): Char {
            return (originalType.code + 100).toChar()
        }

        private fun toOriginalType(checkboxType: Char): Char {
            if (checkboxType.code < 128) {
                return checkboxType
            }
            return (checkboxType.code - 100).toChar()
        }
    }
}