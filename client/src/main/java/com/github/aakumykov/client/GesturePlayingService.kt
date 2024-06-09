package com.github.aakumykov.client

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.client.utils.NotificationChannelHelper
import com.github.aakumykov.common.dateTimeString

class GesturePlayingService : AccessibilityService() {

    // TODO: убрать by lazy для ускорения работы

    /*private val pendingContentIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            this,
            CODE_ACTION_STOP,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
    }*/

    private val stopServiceIntent: Intent by lazy {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    private val stopServicePendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            applicationContext,
            CODE_ACTION_STOP,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val stopServiceAction: NotificationCompat.Action by lazy {
        NotificationCompat.Action(
            R.drawable.ic_gesture_playing_service_stop,
            getString(R.string.gesture_playing_service_action_stop),
            stopServicePendingIntent
        )
    }

    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        debugStartStop("onCreate()")
        prepareNotificationChannel()
        prepareNotification()
        showDutyNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.also {
            if (ACTION_STOP == it)
                openAccessibilitySettings()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        debugStartStop("onDestroy()")
        hideDutyNotification()
    }

    private fun showDutyNotification() {
        startForeground(notificationId, dutyNotification)
    }

    private fun hideDutyNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun prepareNotification() {
        notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_gesture_playing_service)
            .setPriority(NOTIFICATION_PRIORITY)
//            .setContentIntent(pendingContentIntent)
            .addAction(stopServiceAction)
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

    private fun debugLog(text: String) { Log.d(TAG, text) }
    private fun debugStartStop(text: String) { Log.d(TAG_START_STOP, text) }

    override fun onInterrupt() {

    }

    private val notificationId: Int = R.id.gesture_playing_service_nitification_id
    private val notificationChannelId: String get() = "${packageName}_notifications"

    companion object {
        val TAG: String = GesturePlayingService::class.java.simpleName
        const val TAG_START_STOP: String = "START_STOP"

        const val CHROME_PACKAGE_NAME = "com.android.chrome"

        // TODO: вынести это в файл настроек?
        const val NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_LOW
        const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_LOW

        const val CODE_ACTION_STOP: Int = 10
        const val ACTION_STOP = "ACTION_STOP"
    }
}

fun AccessibilityEvent.isWindowStateChanged(): Boolean {
    return AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType
}

fun AccessibilityService.isAppWindow(checkedPackageName: String): Boolean {
    return rootInActiveWindow?.packageName?.let { it == checkedPackageName } ?: false
}