package org.yangdai.kori.presentation.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 管理应用锁解锁状态的全局单例类
 */
object AppLockManager {

    // 当前应用锁解锁状态
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    /**
     * 解锁应用
     */
    fun unlock() {
        _isUnlocked.value = true
    }

    /**
     * 锁定应用
     */
    fun lock() {
        _isUnlocked.value = false
    }
}
