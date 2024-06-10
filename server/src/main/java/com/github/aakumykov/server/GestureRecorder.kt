package com.github.aakumykov.server

import android.util.Log
import android.view.MotionEvent
import com.github.aakumykov.kotlin_playground.UserGesturePoint
import com.github.aakumykov.kotlin_playground.UserGesture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class GestureRecorder @Inject constructor() {

    private var initialEvent: MotionEvent? = null
    private var pointList: MutableList<UserGesturePoint> = ArrayList()
    private var startingTime: Long? = null
    private var laseRecordedGesture: UserGesture? = null

    private val _recorderGestureFlow: MutableSharedFlow<UserGesture?> = MutableSharedFlow(0,0)
    val recordedGestureFlow: SharedFlow<UserGesture?> = _recorderGestureFlow

    fun startRecording(initialMotionEvent: MotionEvent) {
        Log.d(TAG, "startRecording(), $initialMotionEvent")
        resetState()
        initialEvent = initialMotionEvent
        startingTime = initialMotionEvent.eventTime
    }

    fun recordEvent(event: MotionEvent) {
        initialEvent?.also {
            pointList.add(UserGesturePoint.fromMotionEvent(it, event))
        }
    }

    fun finishRecording(endingMotionEvent: MotionEvent) {
        Log.d(TAG, "finishRecording(), $endingMotionEvent")

        recordEvent(endingMotionEvent)
        composeUserGesture(endingMotionEvent.eventTime)
        publishLastGesture()
        eraseTempData()
    }

    private fun publishLastGesture() {
        CoroutineScope(Dispatchers.Main).launch {
            _recorderGestureFlow.emit(laseRecordedGesture)
        }
    }

    fun cancelRecording() {
        resetState()
    }


    private fun composeUserGesture(endingEventTime: Long) {
        startingTime?.also {
            laseRecordedGesture = UserGesture.create(pointList, it, endingEventTime)
        }
    }


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

    companion object {
        val TAG: String = GestureRecorder::class.java.simpleName
    }
}