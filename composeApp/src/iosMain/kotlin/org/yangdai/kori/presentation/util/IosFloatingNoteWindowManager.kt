package org.yangdai.kori.presentation.util

class IosFloatingNoteWindowManager : FloatingNoteWindowManager {
    override fun isFloatingWindowSupported(): Boolean {
        return false
    }
}