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
import com.github.aakumykov.server.state.ServerState
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
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.util.Collections
import java.util.UUID
import javax.inject.Inject

class GestureServer @Inject constructor(
    private val gson: Gson,
    private val gestureLogWriter: GestureLogWriter,
    private val timestampSupplier: TimestampSupplier
) {
    val state: StateFlow<ServerState> get() = _state
    private val _state: MutableStateFlow<ServerState> = MutableStateFlow(ServerState.Stopped)

    private suspend fun publishState(serverState: ServerState) {
        _state.emit(serverState)
    }

    private val sessionsMap = Collections.synchronizedMap(HashMap<String,WebSocketServerSession>())
    private val clientIsOnPauseMap = Collections.synchronizedMap(HashMap<String,Boolean>())
    private val scrolledAppIsActiveOnClientMap = Collections.synchronizedMap(HashMap<String,Boolean>())
    
    private fun shouldTransmitGestures(clientId: String): Boolean {
        val clientIsPaused = clientIsOnPauseMap[clientId] ?: false
        val scrolledAppIsActive = scrolledAppIsActiveOnClientMap[clientId] ?: false
        return !clientIsPaused && scrolledAppIsActive
    }

    private var runningServer: ApplicationEngine? = null


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
                        Log.e(TAG, "ОШИБКА: "+ExceptionUtils.getErrorMessage(t), t)
                    }
                }
            }
        }.start(wait = false)

        publishState(ServerState.Started)
    }

    private suspend fun closeSession(connectionId: String, reasonMessage: String) {
        Log.d(TAG, "closeSession(id: connectionId, причина: '$reasonMessage')")
        sessionsMap[connectionId]?.apply {
            close(CloseReason(CloseReason.Codes.NORMAL, reasonMessage))
            sessionsMap.remove(connectionId)
        }

    }


    private suspend fun processTextFrame(clientId: String, textFrame: Frame.Text) {
        textFrame.readText().also { text ->
            when(text) {
                CLIENT_WANTS_TO_DISCONNECT -> closeSession(clientId, CLIENT_WANTS_TO_DISCONNECT)
                CLIENT_WANTS_TO_PAUSE -> { processPauseRequest(clientId) }
                CLIENT_WANTS_TO_RESUME -> { processResumeRequest(clientId) }
                TARGET_APP_IS_ACTIVE -> { onScrolledAppActivated(clientId) }
                TARGET_APP_IS_INACTIVE -> { onScrolledAppDeactivated(clientId) }
                else -> { processLogMessage(text) }
            }
        }
    }

    private suspend fun processLogMessage(text: String) {

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

        gestureLogWriter.writeToLog(logMessage)
    }

    private fun onScrolledAppActivated(clientId: String) {
        Log.d(TAG, "onTargetAppActivated()")
        scrolledAppIsActiveOnClientMap[clientId] = true
    }

    private fun onScrolledAppDeactivated(clientId: String) {
        Log.d(TAG, "onTargetAppDeactivated()")
        scrolledAppIsActiveOnClientMap[clientId] = false
    }

    private suspend fun processResumeRequest(clientId: String) {
        clientIsOnPauseMap[clientId] = false
        sendText(clientId, SERVER_RESUMED)
        publishState(ServerState.Started)
    }

    private suspend fun processPauseRequest(clientId: String) {
        clientIsOnPauseMap[clientId] = true
        sendText(clientId, SERVER_PAUSED)
        publishState(ServerState.Paused)
    }

    private suspend fun sendText(clientId: String, text: String) {
        sessionsMap[clientId]?.apply {
            try {
               outgoing.send(Frame.Text(text))
            } catch (e: Exception) {
                Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            }
        }
    }


    suspend fun sendUserGesture(gesture: UserGesture) {
        Log.d(TAG, "sendUserGesture(), $gesture")

        sessionsMap.keys.forEach { clientId ->  
            if (shouldTransmitGestures(clientId)) {
                sendText(clientId, gson.toJson(gesture))
                Log.d(TAG, "Жест отправлен клиенту $clientId")
            }
        }
    }


    // TODO: что с сессиями?
    suspend fun stop(gracePeriodMillis: Long = 1000, timeoutMillis: Long = 2000) {

        publishState(ServerState.StoppingNow)
        Log.d(TAG, "Останов сервера с ожиданием $timeoutMillis мс...")

        closeAllSessions()

        runningServer?.stop(gracePeriodMillis, timeoutMillis)
        delay(gracePeriodMillis + timeoutMillis)
        runningServer = null

        publishState(ServerState.Stopped)
        Log.d(TAG, "...сервер остановлен.")
    }

    private suspend fun closeAllSessions() {
        sessionsMap.values.forEach { session ->
            session.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Сервер завершает работу"))
        }
    }

    suspend fun notRunningNow(): Boolean {
        return runBlocking {
            state.firstOrNull() !is ServerState.Started
        }
    }


    companion object {
        val TAG: String = GestureServer::class.java.simpleName
    }
}
