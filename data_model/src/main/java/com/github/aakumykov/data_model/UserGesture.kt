package com.github.aakumykov.kotlin_playground

import android.accessibilityservice.GestureDescription
import android.graphics.Path

class UserGesture private constructor(
    private val pointList: List<UserGesturePoint>,
    private val startingTime: Long,
    private val endingTime: Long
) {
    fun createGestureDescription(): GestureDescription? {
        return createStrokeDescription()?.let { stroke ->
            GestureDescription.Builder().apply {
                addStroke(stroke)
            }.build()
        }
    }

    private fun createPath(): Path? {

        if (pointList.size <= 1)
            return null

        return Path().apply {
            pointList.first().also {
                moveTo(it.fromX, it.fromY)
            }
            pointList.subList(1, pointList.lastIndex).forEach { gp ->
                lineTo(gp.toX, gp.toY)
            }
        }
    }

    private fun createStrokeDescription(): GestureDescription.StrokeDescription? {
        return createPath()?.let { path ->
            return GestureDescription.StrokeDescription(
                path,
                0L,
                endingTime - startingTime
            )
        }
    }

    override fun toString(): String {
        return UserGesture::class.java.simpleName + " { ${pointList.size} точек, длительностью ${endingTime-startingTime} мс }"
    }

    companion object {
        fun create(pointList: List<UserGesturePoint>, startingTime: Long, endingTime: Long): UserGesture? {
            return if (pointList.size > 1) {
                UserGesture(
                    ArrayList(pointList),
                    startingTime,
                    endingTime
                )
            } else null
        }
    }
}