package org.yangdai.kori.presentation.util

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 管理应用锁解锁状态的全局单例类
 */
object AppLockManager {

    // 当前应用锁解锁状态
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // 记录上次解锁时间，用于自动锁定功能
    private var _lastUnlockTime = mutableStateOf(0L)
    val lastUnlockTime: Long get() = _lastUnlockTime.value

    /**
     * 解锁应用
     */
    @OptIn(ExperimentalTime::class)
    fun unlock() {
        _isUnlocked.value = true
        _lastUnlockTime.value = Clock.System.now().toEpochMilliseconds()
    }

    /**
     * 锁定应用
     */
    fun lock() {
        _isUnlocked.value = false
    }
}
