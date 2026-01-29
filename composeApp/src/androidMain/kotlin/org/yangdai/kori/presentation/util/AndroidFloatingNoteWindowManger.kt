package org.yangdai.kori.presentation.util

class AndroidFloatingNoteWindowManger : FloatingNoteWindowManager {
    override fun isFloatingWindowSupported(): Boolean {
        return true
    }
}