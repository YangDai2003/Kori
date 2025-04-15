package commonmark.commonmark.node

/**
 * A node that uses delimiters in the source form (e.g. <code>*bold*</code>).
 */
interface Delimited {

    /**
     * @return the opening (beginning) delimiter, e.g. `*`
     */
    fun getOpeningDelimiter(): String

    /**
     * @return the closing (ending) delimiter, e.g. `*`
     */
    fun getClosingDelimiter(): String
}