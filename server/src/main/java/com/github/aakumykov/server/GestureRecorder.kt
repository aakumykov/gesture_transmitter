package com.github.aakumykov.server

import android.view.MotionEvent
import com.github.aakumykov.kotlin_playground.UserGesturePoint
import com.github.aakumykov.kotlin_playground.Gesture
import com.github.aakumykov.single_live_event.SingleLiveEvent

object GestureRecorder {

    private var initialEvent: MotionEvent? = null
    private var pointList: MutableList<UserGesturePoint> = ArrayList()
    private var startingTime: Long? = null
    private var laseRecordedGesture: Gesture? = null

    val recordedGesture: SingleLiveEvent<Gesture> = SingleLiveEvent()

    fun startRecording(initialMotionEvent: MotionEvent) {
        resetState()
        initialEvent = initialMotionEvent
        startingTime = initialMotionEvent.eventTime
    }

    fun recordEvent(event: MotionEvent) {
        initialEvent?.also {
            pointList.add(UserGesturePoint.fromMotionEvent(it, event))
        }
    }

    fun finishRecording(event: MotionEvent) {
        recordEvent(event)
        if (null != startingTime)
                laseRecordedGesture = Gesture.create(pointList, startingTime!!, event.eventTime)
        eraseTempData()
        recordedGesture.value = laseRecordedGesture
    }

    fun cancelRecording() {
        resetState()
    }

    fun getLastRecord(): Gesture? = laseRecordedGesture

    private fun eraseTempData() {
        startingTime = null
        initialEvent = null
        pointList.clear()
    }

    private fun eraseLaseRecord() {
        laseRecordedGesture = null
    }

    private fun resetState() {
        eraseLaseRecord()
        eraseTempData()
    }
}