package com.github.aakumykov.client

import android.net.http.HttpResponseCache.install
import android.util.Log
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class KtorClient {

    private var clientWebSocketSession: ClientWebSocketSession? = null

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

    suspend fun init(
        serverAddress: String,
        serverPort: Int,
        serverPath: String,
    ): Result<KtorClient> {
        return try {
            clientWebSocketSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = serverPath
            )
            Log.d(TAG, "$client")
            Log.d(TAG, "$clientWebSocketSession")
            Result.success(this)

        } catch (e: Exception) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
            return Result.failure(e)
        }
    }

    companion object {
        val TAG: String = KtorClient::class.java.simpleName
    }
}