package com.github.aakumykov.server

import android.util.Log
import com.github.aakumykov.common.CLIENT_WANTS_TO_DISCONNECT
import com.github.aakumykov.common.CLIENT_WANTS_TO_PAUSE
import com.github.aakumykov.common.CLIENT_WANTS_TO_RESUME
import com.github.aakumykov.common.SERVER_PAUSED
import com.github.aakumykov.common.SERVER_RESUMED
import com.github.aakumykov.common.TARGET_APP_IS_ACTIVE
import com.github.aakumykov.common.TARGET_APP_IS_INACTIVE
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocketRaw
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import java.time.Duration
import javax.inject.Inject

class GestureServer @Inject constructor(private val gson: Gson) {

    private var onPause: Boolean = false

    private var targetAppIsActive: Boolean = false

    private val shouldTransmitGestures: Boolean
        get() = (!onPause && targetAppIsActive)

    private var runningServer: ApplicationEngine? = null

    private var serverSession: WebSocketServerSession? = null

    private val sessionHashCode: String
        get() = "session.hashCode: [${(serverSession?.hashCode() ?: "нет сессии")}]"


    suspend fun start(address: String, port: Int, path: String) {

        if (runningServer != null) {
            Log.w(TAG, "Сервер уже запущен")
            return
        } else {
            Log.d(TAG, "Запуск сервера на $address:$port/$path")
        }

        runningServer = embeddedServer(
            CIO,
            host = address,
            port = port
        ) {

            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                webSocketRaw(path = path) {

                    serverSession = this
                    Log.d(TAG, "Новое подключение $sessionHashCode")

                    try {
                        for (frame in incoming) {

                            (frame as? Frame.Close)?.also {
                                closeSession("Пришёл Close-пакет")
                            }

                            (frame as? Frame.Text)?.also {
                                processTextFrame(it)
                            }
                        }
                    }
                    catch (e: ClosedReceiveChannelException) {
                        Log.d(TAG, "Закрытие соединения")
                    }
                    catch (t: Throwable) {
                        Log.e(TAG, "ОШИБКА: "+ExceptionUtils.getErrorMessage(t), t);
                    }
                }
            }
        }.start(wait = true)
    }

    private suspend fun closeSession(reasonMessage: String) {
        Log.d(TAG, "closeSession('$reasonMessage')")
        serverSession?.close(CloseReason(CloseReason.Codes.NORMAL, reasonMessage))
        serverSession = null
    }


    private suspend fun processTextFrame(textFrame: Frame.Text) {
        textFrame.readText().also { text ->
            when(text) {
                CLIENT_WANTS_TO_DISCONNECT -> closeSession(CLIENT_WANTS_TO_DISCONNECT)
                CLIENT_WANTS_TO_PAUSE -> { processPauseRequest() }
                CLIENT_WANTS_TO_RESUME -> { processResumeRequest() }
                TARGET_APP_IS_ACTIVE -> { onTargetAppActivated() }
                TARGET_APP_IS_INACTIVE -> { onTargetAppDeactivated() }
                else -> Log.d(TAG, "Сервер получил текстовое сообщение: '$text'")
            }
        }
    }

    private fun onTargetAppActivated() {
        Log.d(TAG, "onTargetAppActivated()")
        targetAppIsActive = true
    }

    private fun onTargetAppDeactivated() {
        Log.d(TAG, "onTargetAppDeactivated()")
        targetAppIsActive = false
    }

    private suspend fun processResumeRequest() {
        onPause = false
        sendText(SERVER_RESUMED)
    }

    private suspend fun processPauseRequest() {
        onPause = true
        sendText(SERVER_PAUSED)
    }

    private suspend fun sendText(text: String) {
        try {
            serverSession?.outgoing?.send(Frame.Text(text))
                ?: throw IllegalStateException("serverSession == null")
        }
        catch (e: Exception) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
        }
    }


    suspend fun sendUserGesture(gesture: UserGesture) {
        Log.d(TAG, "sendUserGesture() [$sessionHashCode], $gesture")

        if (shouldTransmitGestures) {
            val gestureJson = gson.toJson(gesture)

            if (null != serverSession) {
                sendText(gestureJson)
                Log.d(TAG, "Жест отправлен: $gesture")
            } else {
                Log.e(TAG, "Жест не отправлен: некому отправлять.")
            }
        }
        else {
            // TODO: писать в журнал
            Log.d(TAG, "Нет условий для передачи жеста: targetAppIsActive=$targetAppIsActive, onPause=$onPause")
        }
    }


    suspend fun stop(gracePeriodMillis: Long = 1000, timeoutMillis: Long = 2000) {

        Log.d(TAG, "Останов сервера с ожиданием ${timeoutMillis} мс...")

        runningServer?.stop(gracePeriodMillis, timeoutMillis)
        delay(gracePeriodMillis + timeoutMillis)
        runningServer = null

        Log.d(TAG, "...сервер, вероятно, остановлен.")
    }



    companion object {
        val TAG: String = GestureServer::class.java.simpleName
    }
}