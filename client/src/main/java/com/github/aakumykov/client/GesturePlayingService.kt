package com.github.aakumykov.client

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.github.aakumykov.common.dateTimeString

class GesturePlayingService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()


    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (null != event) {
            if (event.isWindowStateChanged()) {
                if (isAppWindow(CHROME_PACKAGE_NAME)) {
                    debugLog("Гоголь Хром детектед, $dateTimeString")
                }
            }
        }

//        rootInActiveWindow?.also { rootView ->

            /*val pn = rootView.packageName
            val chCnt = rootView.childCount

            val evTypeString = when(event?.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val et = "WINDOW_STATE_CHANGED"
                    debugLog("$et: package: $pn")
                    et
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
                else -> "Другое событие"
            }*/
//            debugLog("--------------")
//            debugLog("$evTypeString: package: $pn, child: $chCnt")
//            showChildrenRecursively(0, rootView.getChildren())
//        }
    }

    private fun debugLog(text: String) {
        Log.d(TAG, text)
    }

    override fun onInterrupt() {

    }

    companion object {
        val TAG: String = GesturePlayingService::class.java.simpleName
        const val CHROME_PACKAGE_NAME = "com.android.chrome"
    }
}

fun AccessibilityEvent.isWindowStateChanged(): Boolean {
    return AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType
}

fun AccessibilityService.isAppWindow(checkedPackageName: String): Boolean {
    return rootInActiveWindow?.packageName?.let { it == checkedPackageName } ?: false
}