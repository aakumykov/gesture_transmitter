package com.github.aakumykov.client.gesture_player

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.client.client_state_provider.KtorStateProvider
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
    private var currentWindowHasContent: Boolean = false


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
        CoroutineScope(Dispatchers.IO).launch {
            gestureClient.userGestures
                .filterNotNull()
                .collect(::onNewUserGesture)
        }
    }


    private fun onNewUserGesture(userGesture: UserGesture) {
        Log.d(TAG, "onNewUserGesture(), $userGesture")
        if (chromeIsRunAndVisible())
            gesturePlayer.playGesture(userGesture)
    }


    private fun chromeIsRunAndVisible(): Boolean {
        return chromeIsLaunched
    }


    // Место для динамической настройки AccessibilityService
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
        if (null != event) {

            if (currentWindowIsChromeWindow()) {
                if (!chromeIsLaunched) {
                    chromeIsLaunched = true
                    reportServerChromeIsActive(chromeIsLaunched)
                }
            } else {
                if (chromeIsLaunched) {
                    chromeIsLaunched = false
                    reportServerChromeIsActive(chromeIsLaunched)
                }
            }

//            chromeIsLaunched = currentWindowIsChromeWindow()
//            currentWindowHasContent = currentWindowHasChild()

            /*debugLog(TAG,
                "Хром "
                    + (if(chromeIsLaunched) "" else "не")
                    + " запущен. "
                    + "Текущее окно "
                    + (if (currentWindowHasContent) "имеет содержимое" else "без содержимого")
                    + "."
            )*/
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
    }

    private fun reportServerChromeIsActive(isActive: Boolean) {
        Log.d(TAG, "reportServerChromeLaunched($isActive)")
        CoroutineScope(Dispatchers.IO).launch {
            gestureClient.reportServerTargetAppIsActive(isActive)
        }
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