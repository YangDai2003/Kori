package commonmark.commonmark.node

/**
 * A fenced code block, e.g.:
 * <pre>
 * ```
 * foo
 * bar
 * ```
 * </pre>
 * <p>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#fenced-code-blocks">CommonMark Spec</a>
 */
class FencedCodeBlock(
    private var fenceCharacter: String = "",
    private var openingFenceLength: Int = 0,
    private var closingFenceLength: Int = 0,
    private var fenceIndent: Int = 0,
    private var info: String = "",
    private var literal: String = "",
) : Block() {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    /**
     * @return the fence character that was used, e.g. {@code `} or {@code ~}, if available, or null otherwise
     */
    fun getFenceCharacter(): String = fenceCharacter

    fun setFenceCharacter(fenceCharacter: String) {
        this.fenceCharacter = fenceCharacter
    }

    /**
     * @return the length of the opening fence (how many of {{@link #getFenceCharacter()}} were used to start the code
     * block) if available, or null otherwise
     */
    fun getOpeningFenceLength(): Int = openingFenceLength

    fun setOpeningFenceLength(openingFenceLength: Int) {
        require(openingFenceLength >= 3) { "openingFenceLength needs to be >= 3" }
        checkFenceLengths(openingFenceLength, closingFenceLength)
        this.openingFenceLength = openingFenceLength
    }

    /**
     * @return the length of the closing fence (how many of {@link #getFenceCharacter()} were used to end the code
     * block) if available, or null otherwise
     */
    fun getClosingFenceLength(): Int = closingFenceLength

    fun setClosingFenceLength(closingFenceLength: Int) {
        require(closingFenceLength >= 3) { "closingFenceLength needs to be >= 3" }
        checkFenceLengths(openingFenceLength, closingFenceLength)
        this.closingFenceLength = closingFenceLength
    }

    fun getFenceIndent(): Int = fenceIndent

    fun setFenceIndent(fenceIndent: Int) {
        this.fenceIndent = fenceIndent
    }

    /**
     * @see <a href="http://spec.commonmark.org/0.31.2/#info-string">CommonMark spec</a>
     */
    fun getInfo(): String = info

    fun setInfo(info: String) {
        this.info = info
    }

    fun getLiteral(): String = literal

    fun setLiteral(literal: String) {
        this.literal = literal
    }

    companion object {
        fun checkFenceLengths(openingFenceLength: Int, closingFenceLength: Int) {
            require(closingFenceLength >= openingFenceLength) { "fence lengths required to be: closingFenceLength >= openingFenceLength" }
        }
    }
}
