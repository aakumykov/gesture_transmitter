package com.github.aakumykov.client.ktor_client

import android.net.Uri
import android.util.Log
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class KtorClient private constructor(
    private val gson: Gson,
    private val ktorStateProvider: KtorStateProvider
): ClientStateProvider by ktorStateProvider {

    private suspend fun publishState(ktorClientState: KtorClientState) {
        KtorStateProvider.setState(ktorClientState)
    }

    private suspend fun publishError(e: Exception) {
        KtorStateProvider.setError(e)
    }

    private var clientWebSocketSession: ClientWebSocketSession? = null
    private var currentServerUri: Uri? = null

    private val client by lazy {
        HttpClient(OkHttp) {
            engine {
                preconfigured = OkHttpClient.Builder()
                    .pingInterval(20, TimeUnit.SECONDS)
                    .build()
            }
            install(WebSockets)
        }
    }


    suspend fun connect(
        serverAddress: String,
        serverPort: Int,
        serverPath: String,
    ): Result<KtorClient> {

        if (isConnected())
            disconnect()

        return try {

            publishState(KtorClientState.CONNECTING)

            clientWebSocketSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = serverPath
            )

            currentServerUri = Uri.parse("ws://$serverAddress:$serverPort/$serverPath")

            Log.d(TAG, "Соединение установлено: $currentServerUri")

            publishState(KtorClientState.RUNNING)

            Result.success(this)

        } catch (e: Exception) {
            publishState(KtorClientState.STOPPED)
            publishError(e)
            return Result.failure(e)
        }
    }


    suspend fun disconnect() {
        try {
            clientWebSocketSession?.close()
            clientWebSocketSession = null
            client.close()
            publishState(KtorClientState.STOPPED)
        } catch (e: Exception) {
            publishState(KtorClientState.ERROR)
            publishError(e)
        }
    }


    fun gesturesFlow(): Flow<UserGesture?>? {
        return clientWebSocketSession?.incoming?.receiveAsFlow()
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
    }

    fun isConnected(): Boolean = null != clientWebSocketSession

    fun isNotConnected(): Boolean = !isConnected()

    fun isNotConnectingNow(): Boolean  = currentStateIs(KtorClientState.CONNECTING)

    fun isNotDisconnectingNow(): Boolean = currentStateIs(KtorClientState.DISCONNECTING)

    private fun currentStateIs(state: KtorClientState): Boolean {
        return KtorStateProvider.getState() != state
    }

    companion object {
        val TAG: String = KtorClient::class.java.simpleName

        private var _ourInstance: KtorClient? = null

        // TODO: заменить на встроенную реализацию Singleton или сделать её через Dagger.
        fun getInstance(gson: Gson, ktorStateProvider: KtorStateProvider): KtorClient {
            if (null == _ourInstance)
                _ourInstance = KtorClient(gson, ktorStateProvider)
            return _ourInstance!!
        }
    }
}