package com.github.aakumykov.server.ktor_server

import androidx.lifecycle.LifecycleOwner
import com.github.aakumykov.kotlin_playground.Gesture
import com.github.aakumykov.server.GestureRecorder
import com.google.gson.Gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.server.jetty.JettyApplicationEngine
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class KtorServer(
    private val lifecycleOwner: LifecycleOwner,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val gson: Gson,
    private val gestureRecorder: GestureRecorder,
) {
    private var runningServer: JettyApplicationEngine? = null

    fun run(address: String, port: Int) {

        coroutineScope.launch(coroutineDispatcher) {

            runningServer = embeddedServer(
                Jetty,
                host = address,
                port = port,
            ) {
                configureWebsockets(
                    coroutineScope = coroutineScope,
                    coroutineDispatcher = coroutineDispatcher,
                    lifecycleOwner = lifecycleOwner,
                    gestureRecorder = gestureRecorder,
                    gson = gson
                )
            }.start(wait = true) // TODO: попробовать wait = false

        }
    }

    fun send(gesture: Gesture) {

    }
}

fun Application.configureWebsockets(
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher,
    lifecycleOwner: LifecycleOwner,
    gestureRecorder: GestureRecorder,
    gson: Gson
) {
    install(WebSockets)

    routing {
        gesturesRoute(
            coroutineScope = coroutineScope,
            coroutineDispatcher = coroutineDispatcher,
            gestureRecorder = gestureRecorder,
            lifecycleOwner = lifecycleOwner,
            gson = gson
        )
    }
}

fun Routing.gesturesRoute(lifecycleOwner: LifecycleOwner,
                          coroutineScope: CoroutineScope,
                          coroutineDispatcher: CoroutineDispatcher,
                          gestureRecorder: GestureRecorder,
                          gson: Gson) {

    webSocket {
        gestureRecorder.recordedGesture.observe(lifecycleOwner) { gesture ->
            val gestureJson = gson.toJson(gesture)
            val textFrame = io.ktor.websocket.Frame.Text(gestureJson)
            coroutineScope.launch(coroutineDispatcher) {
                outgoing.send(textFrame)
            }
        }
    }
}