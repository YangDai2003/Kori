package commonmark.commonmark.text

import kotlin.text.iterator

/**
 * Char matcher that can match ASCII characters efficiently.
 */
class AsciiMatcher private constructor(private val asciiArray: BooleanArray) : CharMatcher {

    override fun matches(c: Char): Boolean {
        return c.code <= 127 && asciiArray[c.code]
    }

    fun newBuilder(): Builder {
        return Builder(asciiArray.copyOf())
    }

    companion object {
        fun builder(): Builder {
            return Builder(BooleanArray(128))
        }

        fun builder(matcher: AsciiMatcher): Builder {
            return Builder(matcher.asciiArray.copyOf())
        }
    }

    class Builder internal constructor(private val asciiArray: BooleanArray) {

        fun c(c: Char): Builder {
            if (c.code > 127) {
                throw IllegalArgumentException("Can only match ASCII characters")
            }
            asciiArray[c.code] = true
            return this
        }

        fun anyOf(s: String): Builder {
            for (c in s) c(c)
            return this
        }

        fun anyOf(characters: Set<Char>): Builder {
            for (c in characters) c(c)
            return this
        }

        fun range(from: Char, toInclusive: Char): Builder {
            var c = from
            while (c <= toInclusive) {
                c(c)
                c = (c.code + 1).toChar()
            }
            return this
        }

        fun build(): AsciiMatcher {
            return AsciiMatcher(asciiArray)
        }
    }
}
