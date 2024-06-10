package com.github.aakumykov.client.ktor_client

import android.util.Log
import com.github.aakumykov.common.CLIENT_WANTS_TO_DISCONNECT
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.cio.webSocketRaw
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch

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

            client.webSocketRaw(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = serverPath
            ) {

                currentSession = this

                publishState(KtorClientState.CONNECTED)

                try {
                    for (frame in incoming) {

                        Log.d(TAG, "FRAME_TYPE (клиент): " + frame.frameType.name)

                        (frame as? Frame.Text)?.also {
                            Log.d(TAG, it.readText())
                        }

                        (frame as? Frame.Close)?.also {
                            publishState(KtorClientState.DISCONNECTED)
                        }
                    }
                }
                catch (e: ClosedReceiveChannelException) {
                    Log.d(TAG, "Попытка чтения из закрытого соединения.")
                    Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
                }
                catch (t: Throwable) {
                    Log.e(TAG, "ОШИБКА: "+ ExceptionUtils.getErrorMessage(t), t)
                }
            }

        } catch (e: Exception) {
            publishError(e)
        }
    }


    private fun startListeningForServer() {
        CoroutineScope(Dispatchers.IO).launch {

            currentSession?.also { session ->

                for (frame in session.incoming) {

                    Log.d(TAG, "FRAME_TYPE (клиент): "+frame.frameType.name)

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



    suspend fun requestDisconnect() {

        if (isConnected()) {
            if (isNotDisconnectingNow() || isNotConnectingNow()) {
                sendTextMessage(CLIENT_WANTS_TO_DISCONNECT)
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
        try {
            currentSession?.send(Frame.Text(text))
                ?: throw IllegalStateException("sendTextMessage(): currentSession is NULL")
        }
        catch (e: Exception) {
            publishError(e)
        }
    }

    suspend fun sendCloseMessage() {
        Log.d(TAG, "sendCloseMessage()")
        try {
            currentSession?.send(Frame.Close())
                ?: throw IllegalStateException("sendCloseMessage(): currentSession is NULL")
        }
        catch (e: Exception) {
            publishError(e)
        }
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