package com.github.aakumykov.client.gesture_client

import android.util.Log
import com.github.aakumykov.client.client_state_provider.ClientState
import com.github.aakumykov.client.client_state_provider.ClientStateProvider
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
import javax.inject.Inject

/**
 * Получает [UserGesture] от GestureServer-а,
 * публикует их для своих пользователей.
 */
class GestureClient @Inject constructor(
    private val gson: Gson,
    private val clientStateProvider: ClientStateProvider,
    private val timestampSupplier: TimestampSupplier
)
    : ClientStateProvider by clientStateProvider
{
    // FIXME: убрать TimestampSupplier

    private var currentSession: ClientWebSocketSession? = null


    private val _userGestureFlow: MutableSharedFlow<UserGesture?> = MutableStateFlow(null)
    val userGestures: SharedFlow<UserGesture?> = _userGestureFlow


    val currentState: ClientState get() = clientStateProvider.getState()

    val currentError: Exception? get() = clientStateProvider.getError()


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
                            processCloseFrame()
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


    suspend fun reportServerTargetAppIsActive(isActive: Boolean) {
        sendTextMessage(
            if (isActive) TARGET_APP_IS_ACTIVE
            else TARGET_APP_IS_INACTIVE
        )
    }



    private suspend fun processCloseFrame() {
        publishState(ClientState.DISCONNECTED)
        currentSession = null
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
            gson.fromJson(text, UserGesture::class.java)?.also { userGesture ->
                Log.d(TAG, userGesture.toString())
                publishUserGesture(userGesture)
                logUserGestureToServer(userGesture)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            logErrorToServer(e)
        }
    }


    private suspend fun logErrorToServer(e: Exception) {
        sendLogMessage(
            LogMessage.create("ОШИБКА РАЗБОРА ЖЕСТА: "+ExceptionUtils.getErrorMessage(e), timestampSupplier.get())
        )
    }


    private suspend fun logUserGestureToServer(userGesture: UserGesture) {
        sendLogMessage(
            LogMessage.create("Клиент получил жест: $userGesture", timestampSupplier.get())
        )
    }

    private suspend fun sendLogMessage(logMessage: LogMessage) {
        sendTextMessage(gson.toJson(logMessage))
    }


    private suspend fun publishUserGesture(userGesture: UserGesture) {
        _userGestureFlow.emit(userGesture)
    }


    private suspend fun publishState(clientState: ClientState) {
        clientStateProvider.setState(clientState)
    }


    private suspend fun publishError(e: Exception) {
        clientStateProvider.setState(ClientState.ERROR)
        clientStateProvider.setError(e)
    }


    fun isConnected(): Boolean = (null != currentSession && currentStateIs(ClientState.CONNECTED))

    private fun isNotConnectingNow(): Boolean  = !currentStateIs(ClientState.CONNECTING)

    private fun isNotDisconnectingNow(): Boolean = !currentStateIs(ClientState.DISCONNECTING)

    private fun currentStateIs(state: ClientState): Boolean {
        return currentState == state
    }

    private suspend fun sendTextMessage(text: String) {
        Log.d(TAG, "sendTextMessage($text)")
        try {
            currentSession?.outgoing?.send(Frame.Text(text))
                ?: throw IllegalStateException("sendTextMessage(): currentSession is NULL")
        }
        catch (e: Exception) {
            publishError(e)
        }
    }

    companion object {
        val TAG: String = GestureClient::class.java.simpleName
    }
}