package com.github.aakumykov.server

import android.util.Log
import com.github.aakumykov.common.constants.CLIENT_WANTS_TO_DISCONNECT
import com.github.aakumykov.common.constants.CLIENT_WANTS_TO_PAUSE
import com.github.aakumykov.common.constants.CLIENT_WANTS_TO_RESUME
import com.github.aakumykov.common.constants.SERVER_PAUSED
import com.github.aakumykov.common.constants.SERVER_RESUMED
import com.github.aakumykov.common.constants.TARGET_APP_IS_ACTIVE
import com.github.aakumykov.common.constants.TARGET_APP_IS_INACTIVE
import com.github.aakumykov.common.utils.TimestampSupplier
import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.kotlin_playground.UserGesture
import com.github.aakumykov.logger.gesture_logger.GestureLogWriter
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Collections
import java.util.UUID
import javax.inject.Inject

class GestureServer @Inject constructor(
    private val gson: Gson,
    private val gestureLogWriter: GestureLogWriter,
    private val timestampSupplier: TimestampSupplier
) {
    private var onPause: Boolean = false

    private var targetAppIsActive: Boolean = false

    private val shouldTransmitGestures: Boolean
        get() = (!onPause && targetAppIsActive)

    private var runningServer: ApplicationEngine? = null

    private val sessionsMap = Collections.synchronizedMap(HashMap<String,WebSocketServerSession>())

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

                    val connectionId: String = this.incoming.hashCode().toString()
                    sessionsMap[connectionId] = this

                    Log.d(TAG, "Новое подключение, id=$connectionId")

                    try {
                        for (frame in incoming) {

                            val incomingConnectionHashCode = incoming.hashCode().toString()
                            Log.d(TAG, incomingConnectionHashCode)

                            (frame as? Frame.Close)?.also {
                                closeSession(connectionId, "Пришёл Close-пакет через соединение $connectionId")
                            }

                            (frame as? Frame.Text)?.also {
                                processTextFrame(connectionId, it)
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

    private suspend fun closeSession(connectionId: String, reasonMessage: String) {
        Log.d(TAG, "closeSession(id: connectionId, причина: '$reasonMessage')")
        sessionsMap[connectionId]?.apply {
            close(CloseReason(CloseReason.Codes.NORMAL, reasonMessage))
            sessionsMap.remove(connectionId)
        }

    }


    private suspend fun processTextFrame(connectionId: String, textFrame: Frame.Text) {
        textFrame.readText().also { text ->
            when(text) {
                CLIENT_WANTS_TO_DISCONNECT -> closeSession(connectionId, CLIENT_WANTS_TO_DISCONNECT)
                CLIENT_WANTS_TO_PAUSE -> { processPauseRequest() }
                CLIENT_WANTS_TO_RESUME -> { processResumeRequest() }
                TARGET_APP_IS_ACTIVE -> { onTargetAppActivated() }
                TARGET_APP_IS_INACTIVE -> { onTargetAppDeactivated() }
                else -> { processLogMessage(text) }
            }
        }
    }

    private fun processLogMessage(text: String) {

        val logMessage: LogMessage = try {
            gson.fromJson(text, LogMessage::class.java)
        }
        catch (e: JsonSyntaxException) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            LogMessage(
                id = UUID.randomUUID().toString(),
                message = text,
                timestamp = timestampSupplier.get()
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            gestureLogWriter.writeToLog(logMessage)
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
        sessionsMap.values.forEach {
            try {
                it?.outgoing?.send(Frame.Text(text))
                    ?: throw IllegalStateException("serverSession == null")
            }
            catch (e: Exception) {
                Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            }
        }
    }


    suspend fun sendUserGesture(gesture: UserGesture) {
        Log.d(TAG, "sendUserGesture(), $gesture")

        if (shouldTransmitGestures) {
            val gestureJson = gson.toJson(gesture)

            if (sessionsMap.isNotEmpty()) {
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
