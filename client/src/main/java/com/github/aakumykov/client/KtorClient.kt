package com.github.aakumykov.client

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

class KtorClient(private val gson: Gson, private val clientStateProvider: KtorStateProvider) {

    private suspend fun publishState(ktorClientState: KtorClientState) {
        clientStateProvider.setState(ktorClientState)
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
            clientWebSocketSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = serverPath
            )

            currentServerUri = Uri.parse("ws://$serverAddress:$serverPort/$serverPath")

            Log.d(TAG, "Соединение установлено: $currentServerUri")

            Result.success(this)

        } catch (e: Exception) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
            return Result.failure(e)
        }
    }

    fun getGesturesFlow(): Flow<UserGesture?>? {
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

    suspend fun disconnect() {
        clientWebSocketSession?.close()
        clientWebSocketSession = null
        client.close()
        Log.d(TAG, "Отсоединён от сервера")
    }

    private fun isConnected(): Boolean = null != clientWebSocketSession

    companion object {
        val TAG: String = KtorClient::class.java.simpleName
    }
}