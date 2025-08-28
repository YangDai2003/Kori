package kmark.lexer

import kmark.IElementType

interface GeneratedLexer {

    val tokenStart: Int

    val tokenEnd: Int
    fun reset(buffer: CharSequence, start: Int, end: Int, initialState: Int)

    fun advance(): IElementType?

    val state: Int
}

fun <E> ArrayList<E>.push(e: E) = add(e)

fun <E> ArrayList<E>.pop(): E = removeAt(lastIndex)