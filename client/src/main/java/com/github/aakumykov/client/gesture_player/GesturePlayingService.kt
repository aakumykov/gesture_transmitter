package com.github.aakumykov.client.gesture_player

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.github.aakumykov.client.di.assisted_factories.GesturePlayerAssistedFactory
import com.github.aakumykov.client.di.interfaces.GestureClientComponentProvider
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.config.GOOGLE_CHROME_PACKAGE_NAME
import com.github.aakumykov.kotlin_playground.UserGesture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject


class GesturePlayingService : AccessibilityService() {

    @Inject
    protected lateinit var gesturePlayerFactory: GesturePlayerAssistedFactory


    @Inject
    protected lateinit var gestureClient: GestureClient


    private val gesturePlayer: GesturePlayer
        get() = gesturePlayerFactory.get(this)


    private var chromeIsLaunched: Boolean = false


    override fun onCreate() {
        super.onCreate()
        debugLog("Служба доступности, onCreate()")

        (application as GestureClientComponentProvider)
            .getGestureClientComponent()
            .injectToGesturePlayingService(this)

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
        }
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


    companion object {
        val TAG: String = GesturePlayingService::class.java.simpleName
    }
}


fun AccessibilityService.isAppWindow(checkedPackageName: String): Boolean {
    return when(rootInActiveWindow) {
        null -> false
        else -> rootInActiveWindow.packageName == checkedPackageName
    }
}