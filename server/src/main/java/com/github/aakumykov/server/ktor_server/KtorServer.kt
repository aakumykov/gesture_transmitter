package com.github.aakumykov.server.ktor_server

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.aakumykov.kotlin_playground.Gesture
import com.google.gson.Gson
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.server.jetty.JettyApplicationEngine
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import java.time.Duration

class KtorServer(private val gson: Gson) {

    private var runningServer: JettyApplicationEngine? = null
    private var serverSession: DefaultWebSocketServerSession? = null

    private var _isRunning: MutableLiveData<Result<Boolean>> = MutableLiveData(Result.success(null != serverSession))
    val isRunning: LiveData<Result<Boolean>> = _isRunning

    suspend fun run(address: String, port: Int) {
//        suspendCoroutine<Boolean> { continuation ->
//            try {
                runningServer = embeddedServer(Jetty, host = address, port = port) {

                    install(WebSockets) {
                        pingPeriod = Duration.ofSeconds(15)
                        timeout = Duration.ofSeconds(15)
                        maxFrameSize = Long.MAX_VALUE
                        masking = false
                    }

                    routing {
                        webSocket(path = "/gestures") {
                            // TODO: выдавать на гора статус "готов"

                            Log.d(TAG, "Новое подключение.")

                            outgoing.send(Frame.Text("Вы подключились к серверу $TAG"))

                            serverSession = this

                            _isRunning.value = Result.success(true)
                        }
                    }
                }.start(wait = true)

//                continuation.resume(true)

          /*  } catch (e: Exception) {
                // FIXME: это сообщение нигде не наблюдается.
                ExceptionUtils.getErrorMessage(e).also { errorMsg ->
                    Log.e(TAG, errorMsg, e)
                    _isRunning.value = Result.failure(e)
                }

//                continuation.resumeWithException(e)
            }*/
//        }
    }

    // TODO: выдавать поток с ошибками
    suspend fun send(gesture: Gesture) {

        val gestureJson = gson.toJson(gesture)
        val textFrame = Frame.Text(gestureJson)

//        try {
            serverSession?.apply {
                outgoing.send(textFrame)
                Log.d(TAG, "Жест отправлен: $gesture")
            } ?: Log.e(TAG, "Жест не отправлен, так как ещё никто не подключился.")

        /*} catch (e: Exception) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
        }*/
    }

    companion object {
        val TAG: String = KtorServer::class.java.simpleName
    }
}