package com.github.aakumykov.server.ktor_server

import android.util.Log
import com.github.aakumykov.common.CLIENT_WANTS_TO_DISCONNECT
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.server.jetty.JettyApplicationEngine
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.server.websocket.webSocketRaw
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class KtorServer(private val gson: Gson) {

    private var runningServer: ApplicationEngine? = null
    private var serverSession: WebSocketServerSession? = null
    private val sessionHashCode: String
        get() = "session.hashCode: ${(serverSession?.hashCode() ?: "[нет сессии]")}"
    private val _stopRequested: AtomicBoolean = AtomicBoolean(false)
    private val stopRequested: Boolean get() = _stopRequested.get()


    suspend fun start(address: String, port: Int, path: String) {

        if (runningServer != null) {
            Log.w(TAG, "Сервер уже запущен")
            return
        } else {
            Log.d(TAG, "Запуск сервера на $address:$port/$path")
        }

        _stopRequested.set(false)

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
                    Log.d(TAG, "Новое подключение, ${serverSession.hashCode()}")

                    try {
                        for (frame in incoming) {
                            Log.d(TAG, "FRAME_TYPE: " + frame.frameType.name)
                            (frame as? Frame.Text)?.also {
                                Log.d(TAG, it.readText())
                            }
                        }
                    }
                    catch (e: ClosedReceiveChannelException) {
                        Log.d(TAG, "Закрытие соединения")
                    }
                    catch (t: Throwable) {
                        Log.e(TAG, "ОШИБКА: "+ExceptionUtils.getErrorMessage(t), t);
                    }

                    /*try {
                        for (frame in incoming) {

                            *//*if (stopRequested) {
                                Log.e(TAG, "Запрошен останов сервера...")
                                closeSession("Останов, запрошенный пользователем")
                                return@webSocket
                            }*//*

                            when (frame.frameType) {
                                FrameType.PING -> debugPing()
                                FrameType.PONG -> debugPong()
                                FrameType.CLOSE -> closeSession("Клиент закрыл соединение")
                                FrameType.TEXT -> processTextFrame(frame as? Frame.Text)
                                FrameType.BINARY -> {}
                            }
                        }
                    }
                    catch (e: ClosedReceiveChannelException) {
                        Log.e(TAG, "Входящее соединение зкрыто.")
                    }
                    catch (t: Throwable) {
                        Log.e(TAG, ExceptionUtils.getErrorMessage(t), t)
                    }*/
                }
            }
        }.start(wait = true)
    }

    private suspend fun closeSession(reasonMessage: String) {
        serverSession?.close(CloseReason(CloseReason.Codes.NORMAL, reasonMessage))
        serverSession = null
    }

    private fun debugPing() {
        Log.d(TAG, "--> Пинг сервера, $sessionHashCode")
    }

    private fun debugPong() {
        Log.d(TAG, "<-- Понг от сервера, $sessionHashCode")
    }

    private suspend fun processTextFrame(textFrame: Frame.Text?) {

        textFrame?.readText()?.equals(CLIENT_WANTS_TO_DISCONNECT)?.equals(true)?.also {
            "Клиент запрашивает отключение".also { reasonMessage ->
                Log.d(TAG, reasonMessage)
                closeSession(reasonMessage)
            }
        }

        /*if (null == textFrame) {
            Log.e(TAG, "textFrame равна  null")
            return
        }

        val incomingText: String = textFrame.readText()

        val gesture: UserGesture? = try {
            gson.fromJson(incomingText, UserGesture::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            null
        }

        serverSession?.outgoing?.send(
            Frame.Text(
                gesture?.let { "Получен жест: $it" }
                    ?: "Получен текст: $incomingText"
            )
        ) ?: Log.e(TAG, "Нет текущей сессии для отправки эхо-ответа.")*/
    }


    // TODO: выдавать поток с ошибками
    suspend fun sendUserGesture(gesture: UserGesture) {

        Log.d(TAG, "sendUserGesture(), $gesture")

        val gestureJson = gson.toJson(gesture)
        val textFrame = Frame.Text(gestureJson)

        serverSession?.apply {
            outgoing.send(textFrame)
            Log.d(TAG, "Жест отправлен: $gesture")
        }
            ?: Log.e(TAG, "Жест не отправлен, так как ещё никто не подключился.")
    }


    suspend fun stop(gracePeriodMillis: Long = 1000, timeoutMillis: Long = 2000) {
        Log.d(TAG, "Останов сервера с ожиданием ${timeoutMillis} мс...")
        _stopRequested.set(true)
        runningServer?.stop(gracePeriodMillis, timeoutMillis)
        delay(gracePeriodMillis + timeoutMillis)
        runningServer = null
        Log.d(TAG, "...сервер, вероятно, остановлен.")
    }

    suspend fun sendTestMessage(text: String) {
        serverSession?.outgoing?.send(Frame.Text(text))
    }

    suspend fun sendCloseMessage() {
        serverSession?.outgoing?.send(Frame.Close(
            CloseReason(CloseReason.Codes.NORMAL, "Тестовое Close-сообщение")
        ))
    }


    companion object {
        val TAG: String = KtorServer::class.java.simpleName
    }
}