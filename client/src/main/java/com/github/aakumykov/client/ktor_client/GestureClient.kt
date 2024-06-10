package com.github.aakumykov.client.ktor_client

import android.util.Log
import com.github.aakumykov.common.CLIENT_WANTS_TO_DISCONNECT
import com.github.aakumykov.common.CLIENT_WANTS_TO_PAUSE
import com.github.aakumykov.common.CLIENT_WANTS_TO_RESUME
import com.github.aakumykov.common.SERVER_PAUSED
import com.github.aakumykov.common.SERVER_RESUMED
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.cio.webSocketRaw
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Получает [UserGesture] от GestureServer-а,
 * публикует их для своих пользователей.
 */
class GestureClient private constructor(
    private val gson: Gson,
    private val ktorStateProvider: KtorStateProvider,
//    private val gestureLogger: GestureLogger
): ClientStateProvider by ktorStateProvider {

    private var currentSession: ClientWebSocketSession? = null

    private val _userGestureFlow: MutableSharedFlow<UserGesture?> = MutableStateFlow(null)
    val userGestures: SharedFlow<UserGesture?> = _userGestureFlow


    private val client by lazy {
        HttpClient(CIO) {
            install(WebSockets)
        }
    }


    suspend fun connect(
        serverAddress: String?,
        serverPort: Int,
        serverPath: String?,
    ) {
        try {

            publishState(ClientState.CONNECTING)

            client.webSocketRaw(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = serverPath
            ) {

                currentSession = this

                publishState(ClientState.CONNECTED)

                try {
                    for (frame in incoming) {

                        Log.d(TAG, "FRAME_TYPE (клиент): " + frame.frameType.name)

                        (frame as? Frame.Text)?.also {
                            processTextFrame(it)
                        }

                        (frame as? Frame.Close)?.also {
                            processCloseFrame(it)
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

    private suspend fun processCloseFrame(closeFrame: Frame.Close) {
        publishState(ClientState.DISCONNECTED)
        currentSession = null
    }


    suspend fun requestDisconnection() {

        if (isConnected()) {
            if (isNotDisconnectingNow() || isNotConnectingNow()) {
                sendTextMessage(CLIENT_WANTS_TO_DISCONNECT)
            } else {
                publishError(IllegalStateException("Клиент подключается или отключается прямо сейчас"))
            }
        } else {
            publishError(IllegalStateException("Клиент не подключен к серверу"))
        }
    }


    suspend fun pauseInteraction() {
        sendTextMessage(CLIENT_WANTS_TO_PAUSE)
    }


    suspend fun resumeInteraction() {
        sendTextMessage(CLIENT_WANTS_TO_RESUME)
    }


    private suspend fun processTextFrame(textFrame: Frame.Text) {
        textFrame.readText().also { text ->
            when(text) {
                SERVER_PAUSED -> publishState(ClientState.PAUSED)
                SERVER_RESUMED -> publishState(ClientState.CONNECTED)
                else -> processSerializedUserGesture(text)
            }
        }
    }


    private suspend fun processSerializedUserGesture(text: String) {
        try {
            gson.fromJson(text, UserGesture::class.java)?.also {userGesture ->
                Log.d(TAG, userGesture.toString())
                publishUserGesture(userGesture)
            }
        } catch (e: JsonSyntaxException) {
            // TODO: отчитаться серверу
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
        }
    }


    private suspend fun publishUserGesture(userGesture: UserGesture) {
        _userGestureFlow.emit(userGesture)
    }


    private suspend fun publishState(clientState: ClientState) {
        KtorStateProvider.setState(clientState)
    }


    private suspend fun publishError(e: Exception) {
        KtorStateProvider.setState(ClientState.ERROR)
        KtorStateProvider.setError(e)
    }


    fun isConnected(): Boolean = (null != currentSession && currentStateIs(ClientState.CONNECTED))

    fun isConnectingNow(): Boolean = currentStateIs(ClientState.CONNECTING)

    fun isNotConnected(): Boolean = !isConnected()

    fun isNotConnectingNow(): Boolean  = !currentStateIs(ClientState.CONNECTING)

    fun isNotDisconnectingNow(): Boolean = !currentStateIs(ClientState.DISCONNECTING)

    private fun currentStateIs(state: ClientState): Boolean {
        return currentState == state
    }

    private suspend fun sendTextMessage(text: String) {
        Log.d(TAG, "sendTextMessage($text)")
        try {
            currentSession?.send(Frame.Text(text))
                ?: throw IllegalStateException("sendTextMessage(): currentSession is NULL")
        }
        catch (e: Exception) {
            publishError(e)
        }
    }



    val currentState: ClientState get() = KtorStateProvider.getState()



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