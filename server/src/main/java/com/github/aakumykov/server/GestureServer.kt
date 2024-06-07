package com.github.aakumykov.server

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.github.aakumykov.kotlin_playground.Gesture

/*interface GestureServer {
    val state: LiveData<Boolean>
    fun shutdown()
}*/

object GestureServer {

    private var isActive: Boolean = false

    fun send(newGesture: Gesture?) {

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