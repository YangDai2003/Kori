package commonmark.commonmark.internal

class BlockContent(initialContent: String = "") {
    private val sb: StringBuilder = StringBuilder(initialContent)

    private var lineCount = 0

    fun add(line: CharSequence) {
        if (lineCount != 0) {
            sb.appendLine()
        }
        sb.append(line)
        lineCount++
    }

    override fun toString(): String = sb.toString()
}
