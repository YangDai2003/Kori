package knet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

class JvmConnectivityObserver : ConnectivityObserver {

    override fun observe(): Flow<ConnectivityObserver.Status> {
        return flowOf(ConnectivityObserver.Status.Connected).distinctUntilChanged()
    }
}
