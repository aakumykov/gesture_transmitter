package com.github.aakumykov.client.gesture_player

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.util.Log
import com.github.aakumykov.kotlin_playground.UserGesture

class GesturePlayer(private val accessibilityService: AccessibilityService) {

    private var lastGesture: UserGesture? = null

    fun playGesture(userGesture: UserGesture) {

        Log.d(TAG, "playGesture(), $userGesture")

        lastGesture = userGesture

        userGesture.createGestureDescription()?.also { gestureDescription ->
            accessibilityService.dispatchGesture(
                gestureDescription,
                gestureResultCallback,
                null
            )
        }
    }


    private val gestureResultCallback: AccessibilityService.GestureResultCallback = object: AccessibilityService.GestureResultCallback() {

        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            Log.d(TAG, "Жест выполнен ($lastGesture)")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            Log.d(TAG, "Жест отменён ($lastGesture)")
        }
    }


    companion object {
        val TAG: String = GesturePlayer::class.java.simpleName
    }
}
