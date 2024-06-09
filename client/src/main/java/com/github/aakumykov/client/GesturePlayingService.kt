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
import com.github.aakumykov.client.extensions.showToast
import com.github.aakumykov.client.utils.NotificationChannelHelper
import com.github.aakumykov.common.dateTimeString
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GesturePlayingService : AccessibilityService() {

    private val ktorClient: KtorClient by lazy {
        KtorClient(Gson(), KtorStateProvider)
    }


    private var isPaused: Boolean = false


    private val thisServiceIntent
        get() = Intent(applicationContext, GesturePlayingService::class.java)


    private val pauseServiceIntent: Intent by lazy {
        thisServiceIntent.apply { action = ACTION_PAUSE }
    }


    private val resumeServiceIntent: Intent by lazy {
        thisServiceIntent.apply { action = ACTION_RESUME }
    }


    // FIXME: прямо или косвенно останавливать службу?
    private val stopServiceIntent: Intent by lazy {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }


    // TODO: FLAG_CANCEL_CURRENT
    private val pauseServicePendingIntent: PendingIntent by lazy {
        PendingIntent.getService(
            applicationContext,
            CODE_ACTION_PAUSE,
            pauseServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val resumeServicePendingIntent: PendingIntent by lazy {
        PendingIntent.getService(
            applicationContext,
            CODE_ACTION_RESUME,
            resumeServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }


    // TODO: FLAG_CANCEL_CURRENT
    private val stopServicePendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            applicationContext,
            CODE_ACTION_STOP,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val pauseServiceAction: NotificationCompat.Action by lazy {
        NotificationCompat.Action(
            R.drawable.ic_gesture_playing_service_pause,
            getString(R.string.gesture_playing_service_action_pause),
            pauseServicePendingIntent
        )
    }

    private val resumeServiceAction: NotificationCompat.Action by lazy {
        NotificationCompat.Action(
            R.drawable.ic_gesture_playing_service_resume,
            getString(R.string.gesture_playing_service_action_resume),
            resumeServicePendingIntent
        )
    }

    private val stopServiceAction: NotificationCompat.Action by lazy {
        NotificationCompat.Action(
            R.drawable.ic_gesture_playing_service_stop,
            getString(R.string.gesture_playing_service_action_stop),
            stopServicePendingIntent
        )
    }

    private fun notificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_gesture_playing_service)
            .setPriority(NOTIFICATION_PRIORITY)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1))
            .addAction(stopServiceAction)
    }

    override fun onCreate() {
        super.onCreate()
        debugStartStop("onCreate()")
        prepareNotificationChannel()
        showWorkingNotification()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action) {
            ACTION_STOP -> { onStopWorkRequested() }
            ACTION_RESUME -> { onResumeWorkRequested() }
            ACTION_PAUSE -> { onPauseWorkRequested() }
            null -> { debugLog("Нет действия в Intent") }
            else -> { errorLog("Неизвестное действие в Intent: '${intent.action}'") }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onPauseWorkRequested() {
        isPaused = true
        showPausedNotification()
    }

    private fun onResumeWorkRequested() {
        isPaused = false
        showWorkingNotification()
    }

    // TODO: реализовать останов и высвобождение клиента
    private fun onStopWorkRequested() {
        isPaused = false
    }

    override fun onDestroy() {
        super.onDestroy()
        debugStartStop("onDestroy()")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ktorClient?.disconnect()
            } catch (e: Exception) {
                ExceptionUtils.getErrorMessage(e).also { errorMsg ->
                    showToast(getString(R.string.gesture_playing_service_error, errorMsg))
                    errorLog(errorMsg, e)
                }
            }
        }

        hideNotification()
    }

    private fun showWorkingNotification() {
        startForeground(notificationId, workingNotification)
    }

    private fun showPausedNotification() {
        startForeground(notificationId, pausedNotification)
    }

    private fun hideNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }


    private val workingNotification: Notification by lazy {
        notificationBuilder()
            .setContentTitle(getString(R.string.gesture_playing_service_duty_notification_title))
            .setContentText(getString(R.string.gesture_playing_service_notification_text_working))
            .addAction(pauseServiceAction)
            .build()
    }

    private val pausedNotification: Notification by lazy {
        notificationBuilder()
            .setContentTitle(getString(R.string.gesture_playing_service_duty_notification_title))
            .setContentText(getString(R.string.gesture_playing_service_notification_text_paused))
            .addAction(resumeServiceAction)
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
    private fun errorLog(text: String) { Log.e(TAG, text) }
    private fun errorLog(text: String, throwable: Throwable) { Log.e(TAG, text, throwable) }

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
        const val CODE_ACTION_PAUSE: Int = 20
        const val CODE_ACTION_RESUME: Int = 30

        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
    }
}

fun AccessibilityEvent.isWindowStateChanged(): Boolean {
    return AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType
}

fun AccessibilityService.isAppWindow(checkedPackageName: String): Boolean {
    return rootInActiveWindow?.packageName?.let { it == checkedPackageName } ?: false
}