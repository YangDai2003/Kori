package commonmark.commonmark.node

/**
 * A source span references a snippet of text from the source input.
 * <p>
 * It has a starting position (line and column index) and a length of how many characters it spans.
 * <p>
 * For example, this CommonMark source text:
 * <pre><code>
 * &gt; foo
 * </code></pre>
 * The [BlockQuote] node would have this source span: line 0, column 0, length 5.
 * <p>
 * The [Paragraph] node inside it would have: line 0, column 2, length 3.
 * <p>
 * If a block has multiple lines, it will have a source span for each line.
 * <p>
 * Note that the column index and length are measured in Java characters (UTF-16 code units). If you're outputting them
 * to be consumed by another programming language, e.g. one that uses UTF-8 strings, you will need to translate them,
 * otherwise characters such as emojis will result in incorrect positions.
 *
 * @since 0.16.0
 */
class SourceSpan private constructor(
    /**
     * @return 0-based line index, e.g. 0 for first line, 1 for the second line, etc
     */
    val lineIndex: Int,

    /**
     * @return 0-based index of column (character on line) in source, e.g. 0 for the first character of a line, 1 for
     * the second character, etc
     */
    val columnIndex: Int,

    /**
     * @return 0-based index in whole input
     * @since 0.24.0
     */
    val inputIndex: Int,

    /**
     * @return length of the span in characters
     */
    val length: Int
) {

    init {
        require(lineIndex >= 0) { "lineIndex $lineIndex must be >= 0" }
        require(columnIndex >= 0) { "columnIndex $columnIndex must be >= 0" }
        require(inputIndex >= 0) { "inputIndex $inputIndex must be >= 0" }
        require(length >= 0) { "length $length must be >= 0" }
    }

    fun subSpan(beginIndex: Int): SourceSpan {
        return subSpan(beginIndex, length)
    }

    fun subSpan(beginIndex: Int, endIndex: Int): SourceSpan {
        require(beginIndex >= 0) { "beginIndex $beginIndex + must be >= 0" }
        require(beginIndex <= length) { "beginIndex $beginIndex must be <= length $length" }
        require(endIndex >= 0) { "endIndex $endIndex + must be >= 0" }
        require(endIndex <= length) { "endIndex $endIndex must be <= length $length" }
        require(beginIndex <= endIndex) { "beginIndex $beginIndex must be <= endIndex $endIndex" }

        if (beginIndex == 0 && endIndex == length) {
            return this
        }
        return SourceSpan(lineIndex, columnIndex + beginIndex, inputIndex + beginIndex, endIndex - beginIndex)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SourceSpan) return false

        return lineIndex == other.lineIndex &&
                columnIndex == other.columnIndex &&
                inputIndex == other.inputIndex &&
                length == other.length
    }

    override fun hashCode(): Int {
        var result = lineIndex
        result = 31 * result + columnIndex
        result = 31 * result + inputIndex
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return "SourceSpan{line=$lineIndex, column=$columnIndex, input=$inputIndex, length=$length}"
    }

    companion object {
        fun of(line: Int, col: Int, input: Int, length: Int): SourceSpan {
            return SourceSpan(line, col, input, length)
        }
    }
}
