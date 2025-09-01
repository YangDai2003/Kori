package knet

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.DISPATCH_QUEUE_SERIAL_WITH_AUTORELEASE_POOL
import platform.darwin.dispatch_queue_create

class IOSConnectivityObserver : ConnectivityObserver {
    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create(
        label = "org.yangdai.kori.networkMonitor",
        attr = DISPATCH_QUEUE_SERIAL_WITH_AUTORELEASE_POOL
    )

    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            nw_path_monitor_set_update_handler(monitor) { path ->
                val status = nw_path_get_status(path)
                when (status) {
                    nw_path_status_satisfied -> launch { send(ConnectivityObserver.Status.Connected) }
                    else -> launch { send(ConnectivityObserver.Status.Disconnected) }
                }
            }

            nw_path_monitor_set_queue(monitor, queue)
            nw_path_monitor_start(monitor)

            awaitClose {
                nw_path_monitor_cancel(monitor)
            }
        }.distinctUntilChanged()
    }
}
