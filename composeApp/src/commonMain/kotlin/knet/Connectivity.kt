package knet

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observe(): Flow<Status>

    enum class Status {
        Connected,     // 网络可用
        Disconnected,   // 网络不可用
    }
}