package com.github.aakumykov.client

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class GesturePlayingService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()


    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        /*event?.also { e ->
            when(e.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> debugWindowEvent(e)
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> debugWindowContentEvent(e)
                else -> {}
            }
        }*/
        rootInActiveWindow?.also { rootView ->
            debugLog("--------------")
            debugLog("rootView.packageName: ${rootView.packageName}, childCount: ${rootView.childCount}")
//            showChildrenRecursively(0, rootView.getChildren())
        }
    }

    private fun debugLog(text: String) {
        Log.d(TAG, text)
    }

    override fun onInterrupt() {

    }

    companion object {
        val TAG: String = GesturePlayingService::class.java.simpleName
    }
}