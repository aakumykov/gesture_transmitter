package com.github.aakumykov.server.ktor_server

import android.util.Log
import com.github.aakumykov.kotlin_playground.UserGesture
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
import io.ktor.websocket.readText
import java.time.Duration

class KtorServer(private val gson: Gson) {

    private var runningServer: JettyApplicationEngine? = null
    private var serverSession: DefaultWebSocketServerSession? = null

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

                    /*serverSession = this
                    Log.d(TAG, "Новое подключение.")*/

                    outgoing.send(Frame.Text("$TAG приветствует вас!"))

                    for (frame in incoming) {
                        (frame as? Frame.Text)?.let { textFrame ->
                            val incomingText = textFrame.readText()
                            outgoing.send(Frame.Text("И вам '$incomingText'"))
                        }
                    }
                }
            }
        }.start(wait = true)
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