package com.github.aakumykov.client

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.aakumykov.client.utils.NotificationChannelHelper
import com.github.aakumykov.common.dateTimeString

class GesturePlayingService : AccessibilityService() {

    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        prepareNotificationChannel()
        prepareNotificationBuilder()
        showDutyNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideDutyNotification()
    }

    private fun showDutyNotification() {
        startForeground(notificationId, dutyNotification)
    }

    private fun hideDutyNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun prepareNotificationBuilder() {
        notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_gesture_playing_service)
            .setPriority(NOTIFICATION_PRIORITY)
    }

    private val dutyNotification: Notification by lazy {
        notificationBuilder
            .setContentTitle(getString(R.string.gesture_playing_service_duty_notification_title))
            .setContentText(getString(R.string.gesture_playing_service_duty_notification_text))
            .build()
    }

    private fun prepareNotificationChannel() {
        NotificationChannelHelper(NotificationManagerCompat.from(this))
            .createNotificationChannel(
                id = notificationChannelId,
                importance = NOTIFICATION_CHANNEL_IMPORTANCE,
                getString(R.string.gesture_playing_service_notification_channel_name),
                getString(R.string.gesture_playing_service_notification_channel_description),
            )
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

    private val notificationId: Int = R.id.gesture_playing_service_nitification_id
    private val notificationChannelId: String get() = "${packageName}_notifications"

    companion object {
        val TAG: String = GesturePlayingService::class.java.simpleName

        const val CHROME_PACKAGE_NAME = "com.android.chrome"

        // TODO: вынести это в файл настроек?
        const val NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_LOW
        const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_LOW
    }
}

fun AccessibilityEvent.isWindowStateChanged(): Boolean {
    return AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType
}

fun AccessibilityService.isAppWindow(checkedPackageName: String): Boolean {
    return rootInActiveWindow?.packageName?.let { it == checkedPackageName } ?: false
}