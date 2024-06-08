package com.github.aakumykov.client

import android.net.Uri
import android.util.Log
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.close
import okhttp3.OkHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit

class KtorClient {

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

    suspend fun disconnect() {
        clientWebSocketSession?.close()
        clientWebSocketSession = null
        Log.d(TAG, "Отсоединён от сервера")
    }

    private fun isConnected(): Boolean = null != clientWebSocketSession

    companion object {
        val TAG: String = KtorClient::class.java.simpleName
    }
}