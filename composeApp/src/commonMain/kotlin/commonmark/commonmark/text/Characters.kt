package commonmark.commonmark.text

/**
 * Functions for finding characters in strings or checking characters.
 */
object Characters {
    fun find(c: Char, s: CharSequence, startIndex: Int): Int {
        val length: Int = s.length
        for (i in startIndex..<length) {
            if (s[i] == c) {
                return i
            }
        }
        return -1
    }

    fun findLineBreak(s: CharSequence, startIndex: Int): Int {
        val length: Int = s.length
        for (i in startIndex..<length) {
            when (s[i]) {
                '\n', '\r' -> return i
            }
        }
        return -1
    }

    /**
     * @see [blank line](https://spec.commonmark.org/0.31.2/.blank-line)
     */
    fun isBlank(s: CharSequence): Boolean {
        return skipSpaceTab(s, 0, s.length) == s.length
    }

    fun hasNonSpace(s: CharSequence): Boolean {
        val length: Int = s.length
        val skipped: Int = skip(' ', s, 0, length)
        return skipped != length
    }

    fun isLetter(s: CharSequence, index: Int): Boolean {
        if (index < s.length) {
            val c: Char = s[index]
            return c.isLetter()
        }
        return false
    }

    fun isSpaceOrTab(s: CharSequence, index: Int): Boolean {
        if (index < s.length) {
            when (s[index]) {
                ' ', '\t' -> return true
            }
        }
        return false
    }

    /**
     * @see [Unicode punctuation character](https://spec.commonmark.org/0.31.2/.unicode-punctuation-character)
     */
    fun isPunctuationCodePoint(codePoint: Int): Boolean {
        return when (codePoint.toChar().category) {
            CharCategory.DASH_PUNCTUATION, CharCategory.START_PUNCTUATION, CharCategory.END_PUNCTUATION, CharCategory.CONNECTOR_PUNCTUATION, CharCategory.OTHER_PUNCTUATION, CharCategory.INITIAL_QUOTE_PUNCTUATION, CharCategory.FINAL_QUOTE_PUNCTUATION, CharCategory.MATH_SYMBOL, CharCategory.CURRENCY_SYMBOL, CharCategory.MODIFIER_SYMBOL, CharCategory.OTHER_SYMBOL -> true
            else -> when (codePoint.toChar()) {
                '$', '+', '<', '=', '>', '^', '`', '|', '~' -> true
                else -> false
            }
        }
    }

    /**
     * Check whether the provided code point is a Unicode whitespace character as defined in the spec.
     *
     * @see [Unicode whitespace character](https://spec.commonmark.org/0.31.2/.unicode-whitespace-character)
     */
    fun isWhitespaceCodePoint(codePoint: Int): Boolean {
        return when (codePoint.toChar()) {
            ' ', '\t', '\n', '\u000c', '\r' -> true
            else -> codePoint.toChar().category === CharCategory.SPACE_SEPARATOR
        }
    }

    fun skip(skip: Char, s: CharSequence, startIndex: Int, endIndex: Int): Int {
        for (i in startIndex..<endIndex) {
            if (s[i] != skip) {
                return i
            }
        }
        return endIndex
    }

    fun skipBackwards(skip: Char, s: CharSequence, startIndex: Int, lastIndex: Int): Int {
        for (i in startIndex downTo lastIndex) {
            if (s[i] != skip) {
                return i
            }
        }
        return lastIndex - 1
    }

    fun skipSpaceTab(s: CharSequence, startIndex: Int, endIndex: Int): Int {
        for (i in startIndex..<endIndex) {
            when (s[i]) {
                ' ', '\t' -> {}
                else -> return i
            }
        }
        return endIndex
    }

    fun skipSpaceTabBackwards(s: CharSequence, startIndex: Int, lastIndex: Int): Int {
        for (i in startIndex downTo lastIndex) {
            when (s[i]) {
                ' ', '\t' -> {}
                else -> return i
            }
        }
        return lastIndex - 1
    }
}
