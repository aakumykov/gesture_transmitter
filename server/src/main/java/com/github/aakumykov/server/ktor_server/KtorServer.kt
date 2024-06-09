package com.github.aakumykov.server.ktor_server

import android.util.Log
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.time.Duration

class KtorServer(private val gson: Gson) {

    private var runningServer: JettyApplicationEngine? = null
    private var serverSession: DefaultWebSocketServerSession? = null
    private val sessionHashCode: String
        get() = "session.hashCode: ${(serverSession?.hashCode() ?: "[нет сессии]")}"

    suspend fun run(address: String, port: Int) {

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

                    serverSession = this
                    Log.d(TAG, "Новое подключение, ${serverSession.hashCode()}")

                    for (frame in incoming) {

                        when(frame.frameType) {
                            FrameType.PING -> debugPing()
                            FrameType.PONG -> debugPong()
                            FrameType.CLOSE -> closeCurrentSession()
                            FrameType.TEXT -> processTextFrame(frame as? Frame.Text)
                            FrameType.BINARY -> {}
                        }
                    }
                }
            }
        }.start(wait = true)
    }

    private fun debugPing() {
        Log.d(TAG, "--> Пинг сервера, $sessionHashCode")
    }

    private fun debugPong() {
        Log.d(TAG, "<-- Понг от сервера, $sessionHashCode")
    }

    private suspend fun processTextFrame(textFrame: Frame.Text?) {

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

    private suspend fun closeCurrentSession() {
        serverSession?.apply {
            val sessionHashCode = this.hashCode()
            close(
                CloseReason(CloseReason.Codes.NORMAL,"Закрыта сессия $sessionHashCode")
            )
        }
    }

    // TODO: выдавать поток с ошибками
    suspend fun send(gesture: UserGesture) {

        val gestureJson = gson.toJson(gesture)
        val textFrame = Frame.Text(gestureJson)

        serverSession?.apply {
            outgoing.send(textFrame)
            Log.d(TAG, "Жест отправлен: $gesture")
        }
            ?: Log.e(TAG, "Жест не отправлен, так как ещё никто не подключился.")
    }



    companion object {
        val TAG: String = KtorServer::class.java.simpleName
    }
}