package com.github.aakumykov.client

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class GesturePlayingService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()


    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }
}