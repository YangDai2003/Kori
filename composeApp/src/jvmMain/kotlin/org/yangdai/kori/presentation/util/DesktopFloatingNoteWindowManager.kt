package org.yangdai.kori.presentation.util

class DesktopFloatingNoteWindowManager : FloatingNoteWindowManager {
    override fun isFloatingWindowSupported(): Boolean {
        return true
    }
}