package com.github.aakumykov.client.gesture_player

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.aakumykov.client.R
import com.github.aakumykov.client.ktor_client.GestureClient
import com.github.aakumykov.client.ktor_client.ClientState
import com.github.aakumykov.client.ktor_client.KtorStateProvider
import com.github.aakumykov.client.settings_provider.SettingsProvider
import com.github.aakumykov.client.utils.NotificationChannelHelper
import com.github.aakumykov.common.GOOGLE_CHROME_PACKAGE_NAME
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


class GesturePlayingService : AccessibilityService() {

    // TODO: добавить ChromeDetector

    private val trackedWindowHasAppeared: AtomicBoolean = AtomicBoolean(false)
    private val trackedWindowNotVisible: Boolean get() = !trackedWindowHasAppeared.get()

    private var chromeIsLaunched: Boolean = false
    private var chromeHasContent: Boolean = false


    private val gesturePlayer: GesturePlayer by lazy {
        GesturePlayer(this)
    }

    private val gestureClient: GestureClient by lazy {
        GestureClient.getInstance(Gson(), KtorStateProvider)
    }


    override fun onCreate() {
        super.onCreate()
        debugLog("Служба доступности, onCreate()")
        prepareGestureClient()
    }

    override fun onDestroy() {
        super.onDestroy()
        debugLog("Служба доступности, onDestroy()")
    }


    override fun onInterrupt() {}


    private fun prepareGestureClient() {
        CoroutineScope(Dispatchers.Main).launch {
            gestureClient.state.collect(::onClientStateChanged)
        }

        CoroutineScope(Dispatchers.IO).launch {
            gestureClient.userGestures.filterNotNull().collect(::onNewUserGesture)
        }
    }


    private fun onNewUserGesture(userGesture: UserGesture) {
        Log.d(TAG, userGesture.toString())
        gesturePlayer.playGesture(userGesture)
    }


    private fun onClientStateChanged(clientState: ClientState) {

        debugLog("Состояние GestureClient-а: $clientState")

        when(clientState) {
            ClientState.INACTIVE -> {}
            ClientState.CONNECTING -> {}
            ClientState.DISCONNECTING -> {}
            ClientState.CONNECTED -> startListeningForGestures()
            ClientState.PAUSED -> {}
            ClientState.DISCONNECTED -> {}
            ClientState.ERROR -> {}
        }
    }

    private fun startListeningForGestures() {
        /*CoroutineScope(Dispatchers.IO).launch {
            try {
                ktorClient.gesturesFlow()?.collect { userGesture: UserGesture? ->
                    gesturePlayer.playGesture(userGesture)
                }
            } catch (e: Exception) {
                errorLog(e)
            }
        }*/
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        /*getServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED //or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf("com.android.chrome")
            // flags = AccessibilityServiceInfo.DEFAULT;
            notificationTimeout = 100
        }.also {
            setServiceInfo(it)
        }*/
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        event?.also { ev ->
            chromeIsLaunched = currentWindowIsChromeWindow()
            chromeHasContent = currentWindowHasChild()

//            debugLog("windows_check","хром запущен: $chromeIsLaunched, содержимое: $chromeHasContent")

            /*CoroutineScope(Dispatchers.IO).launch {
                if (chromeIsLaunched && chromeHasContent) {
                    if (ktorClient.isNotConnected() && ktorClient.isNotConnectingNow())
                        connectToServer()
                } else {
                    if (ktorClient.isConnected() && ktorClient.isNotDisconnectingNow())
                        disconnectFromServer()
                }
            }*/
        }

        /*if (null != event && event.isWindowStateChanged()) {
            if (isAppWindow(GOOGLE_CHROME_PACKAGE_NAME, true)*//* && trackedWindowNotVisible*//*) {
                debugLog("Гоголь Хром детектед, $dateTimeString")
                trackedWindowHasAppeared.set(true)
                connectToServer()
            } else {
                debugLog("Гоголь Хром ушёл в фон, $dateTimeString")
                trackedWindowHasAppeared.set(false)
                disconnectFromServer()
            }
        }*/

        /*rootInActiveWindow?.also { rootView ->

            val pn = rootView.packageName
            val chCnt = rootView.childCount

            val evTypeString = when(event?.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val et = "WINDOW_STATE_CHANGED"
                    debugLog("$et: package: $pn")
                    et
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
                else -> "Другое событие"
            }

            debugLog("window_state","--------------")
            debugLog("window_state","$evTypeString: package: $pn, child: $chCnt")
//            showChildrenRecursively(0, rootView.getChildren())
        }*/
    }

    private fun currentWindowIsChromeWindow(): Boolean {
        return isAppWindow(GOOGLE_CHROME_PACKAGE_NAME)
    }



    private fun debugLog(text: String) { Log.d(TAG, text) }
    private fun debugLog(tag: String, text: String) { Log.d(tag, text) }
    private fun errorLog(text: String) { Log.e(TAG, text) }
    private fun errorLog(throwable: Throwable) { Log.e(TAG, ExceptionUtils.getErrorMessage(throwable), throwable) }
    private fun errorLog(text: String, throwable: Throwable) { Log.e(TAG, text, throwable) }


    companion object {
        val TAG: String = GesturePlayingService::class.java.simpleName
    }
}

fun AccessibilityEvent.isWindowStateChanged(): Boolean {
    return AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType
}


fun AccessibilityService.isAppWindow(checkedPackageName: String): Boolean {
    return when(rootInActiveWindow) {
        null -> false
        else -> rootInActiveWindow.packageName == checkedPackageName
    }
}

fun AccessibilityService.currentWindowHasChild(): Boolean {
    return when(rootInActiveWindow) {
        null -> false
        else -> rootInActiveWindow.childCount > 0
    }
}