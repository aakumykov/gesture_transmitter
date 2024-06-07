package com.github.aakumykov.kotlin_playground

import android.view.MotionEvent

data class UserGesturePoint(
    val fromX: Float,
    val fromY: Float,
    val toX: Float,
    val toY: Float,
) {
    companion object {
        fun fromMotionEvent(e1: MotionEvent, e2: MotionEvent): UserGesturePoint {
            return UserGesturePoint(
                fromX = e1.rawX,
                fromY = e1.rawY,
                toX = e2.rawX,
                toY = e2.rawY
            )
        }
    }
}
