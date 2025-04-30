package kmark.parser

import kmark.ExperimentalApi

@ExperimentalApi
fun interface CancellationToken {
    fun checkCancelled()

    object NonCancellable : CancellationToken {
        override fun checkCancelled() = Unit
    }
}
