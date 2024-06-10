package com.github.aakumykov.client.ktor_client

import android.util.Log
import com.github.aakumykov.common.CLIENT_WANTS_TO_DISCONNECT
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Подключается, отключается, слушает GestureServer.
 * Публикует полученные "жесты" в виде потока.
 */
class GestureClient private constructor(
    private val gson: Gson,
    private val ktorStateProvider: KtorStateProvider
): ClientStateProvider by ktorStateProvider {

    private suspend fun publishState(ktorClientState: KtorClientState) {
        KtorStateProvider.setState(ktorClientState)
    }

    private suspend fun publishError(e: Exception) {
        KtorStateProvider.setState(KtorClientState.ERROR)
        KtorStateProvider.setError(e)
    }

    private var currentSession: ClientWebSocketSession? = null


    private val client by lazy {
        HttpClient(CIO) {
            /*engine {
                preconfigured = OkHttpClient.Builder()
                    .pingInterval(20, TimeUnit.SECONDS)
                    .build()
            }*/
            install(WebSockets)
        }
    }


    suspend fun connect(
        serverAddress: String?,
        serverPort: Int,
        serverPath: String?,
    ) {
        try {

            publishState(KtorClientState.CONNECTING)

            client.webSocket(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = serverPath
            ) {
                currentSession = this

                startListeningForServer()
            }

            publishState(KtorClientState.CONNECTED)

        } catch (e: Exception) {
            publishError(e)
        }
    }


    private fun startListeningForServer() {
        CoroutineScope(Dispatchers.IO).launch {

            currentSession?.also { session ->

                for (frame in session.incoming) {

                    Log.d("FRAME_TYPE", frame.frameType.name)

                    when (frame.frameType) {
                        FrameType.PING -> {  }
                        FrameType.PONG -> {  }
                        FrameType.TEXT -> {  }
                        FrameType.BINARY -> {  }
                        FrameType.CLOSE -> {  }
                    }
                }
            }
        }
    }


    suspend fun disconnect() {
        try {
            publishState(KtorClientState.DISCONNECTING)

            currentSession?.close()
            currentSession = null
            client.close()

            publishState(KtorClientState.DISCONNECTED)

        } catch (e: Exception) {
            publishError(e)
        }
    }


    suspend fun requestDisconnect() {

        if (isConnected()) {
            if (isNotDisconnectingNow() || isNotConnectingNow()) {
                currentSession?.send(CLIENT_WANTS_TO_DISCONNECT)
            } else {
                publishError(IllegalStateException("Клиент подключается или отключается прямо сейчас"))
            }
        } else {
            publishError(IllegalStateException("Клиент не подключен к серверу"))
        }

            /*clientWebSocketSession?.close()
            clientWebSocketSession = null
            client.close()*/

//            publishState(KtorClientState.DISCONNECTED)

//        } catch (e: Exception) {
//            publishError(e)
//        }
    }


    /*fun gesturesFlow(): Flow<UserGesture?>? {
        return webSocketSession?.incoming?.receiveAsFlow()
            ?.filter { it is Frame.Text }
            ?.map { it as Frame.Text }
            ?.map { textFrame ->
                return@map try {
                    val json = textFrame.readText()
                    gson.fromJson(json, UserGesture::class.java).also { userGesture ->
                        Log.d(TAG, "Получен жест: $userGesture")
                    }
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
                    null
                }
            }
    }*/

    fun isConnected(): Boolean = (null != currentSession && currentStateIs(KtorClientState.CONNECTED))

    fun isConnectingNow(): Boolean = currentStateIs(KtorClientState.CONNECTING)

    fun isNotConnected(): Boolean = !isConnected()

    fun isNotConnectingNow(): Boolean  = !currentStateIs(KtorClientState.CONNECTING)

    fun isNotDisconnectingNow(): Boolean = !currentStateIs(KtorClientState.DISCONNECTING)

    private fun currentStateIs(state: KtorClientState): Boolean {
        return currentState == state
    }

    suspend fun sendTextMessage(text: String) {
        Log.d(TAG, "sendTextMessage($text)")
        currentSession?.send(Frame.Text(text)) ?: Log.e(TAG, "sendTextMessage(): currentSession is NULL")
    }

    suspend fun sendCloseMessage() {
        Log.d(TAG, "sendCloseMessage()")
        currentSession?.send(Frame.Close()) ?: Log.e(TAG, "sendCloseMessage(): currentSession is NULL")
    }

    val currentState: KtorClientState get() = KtorStateProvider.getState()



    companion object {
        val TAG: String = GestureClient::class.java.simpleName

        private var _ourInstance: GestureClient? = null

        // TODO: заменить на встроенную реализацию Singleton или сделать её через Dagger.
        fun getInstance(gson: Gson, ktorStateProvider: KtorStateProvider): GestureClient {
            if (null == _ourInstance)
                _ourInstance = GestureClient(gson, ktorStateProvider)
            return _ourInstance!!
        }
    }
}