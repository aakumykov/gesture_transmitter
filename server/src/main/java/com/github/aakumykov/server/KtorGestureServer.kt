package com.github.aakumykov.server

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.github.aakumykov.kotlin_playground.Gesture
import com.github.aakumykov.server.ktor_server.DEFAULT_SERVER_ADDRESS
import com.github.aakumykov.server.ktor_server.DEFAULT_SERVER_PORT
import com.github.aakumykov.server.ktor_server.KtorServer
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.eclipse.jetty.util.HostPort

/*interface KtorGestureServer {
    val state: LiveData<Boolean>
    fun shutdown()
}*/

object KtorGestureServer {

    private var isActive: Boolean = false
    private var ktorServer: KtorServer? = null

    fun send(newGesture: Gesture?) {

    }

    fun init(
        serverAddress: String,
        serverPort: Int,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
        gson: Gson,
        gestureRecorder: GestureRecorder
    ): KtorGestureServer {
        ktorServer = KtorServer(
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            coroutineDispatcher = coroutineDispatcher,
            gson = gson,
            gestureRecorder = gestureRecorder
        ).apply {
            run(serverAddress, serverPort)
        }
        return this
    }

    private val _stateLiveData: MutableLiveData<Boolean> = MediatorLiveData(false)

    val state: LiveData<Boolean>
        get() = _stateLiveData

//    private var serverSession: DefaultWebSocketServerSession? = null

    // fixme: этот метод нарушает контракт
    /*fun init(serverSession: DefaultWebSocketServerSession) {
        this.serverSession = serverSession
    }*/


}