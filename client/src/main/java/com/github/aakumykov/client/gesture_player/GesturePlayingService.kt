package com.github.aakumykov.client.gesture_player

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.aakumykov.client.R
import com.github.aakumykov.client.ktor_client.GestureClient
import com.github.aakumykov.client.ktor_client.KtorClientState
import com.github.aakumykov.client.ktor_client.KtorStateProvider
import com.github.aakumykov.client.settings_provider.SettingsProvider
import com.github.aakumykov.client.utils.NotificationChannelHelper
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class GesturePlayingService : AccessibilityService() {

    private val trackedWindowHasAppeared: AtomicBoolean = AtomicBoolean(false)
    private val trackedWindowNotVisible: Boolean get() = !trackedWindowHasAppeared.get()

    private var chromeIsLaunched: Boolean = false
    private var chromeHasContent: Boolean = false


    private val settingsProvider: SettingsProvider by lazy {
        SettingsProvider.getInstance(applicationContext)
    }

    private val gesturePlayer: GesturePlayer by lazy {
        GesturePlayer(this)
    }

    private val serverAddress: String? get() = settingsProvider.getIpAddress()
    private val serverPort: Int get() = settingsProvider.getPort()
    private val serverPath: String? get() = settingsProvider.getPath()


    private val ktorClient: GestureClient by lazy {
        GestureClient.getInstance(Gson(), KtorStateProvider)
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
        debugLog("Служба доступности, onCreate()")
//        prepareKtorClient()
//        connectToServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        debugLog("Служба доступности, onDestroy()")
//        disconnectFromServer()
//        hideNotification()
    }

    private fun prepareKtorClient() {
        CoroutineScope(Dispatchers.Main).launch {
            ktorClient.state.collect(::onClientStateChanged)
        }
    }

    private fun onClientStateChanged(ktorClientState: KtorClientState) {

        debugLog("Состояние Ktor-клиента: $ktorClientState")

        when(ktorClientState) {
            KtorClientState.INACTIVE -> {}
            KtorClientState.CONNECTING -> {}
            KtorClientState.DISCONNECTING -> {}
            KtorClientState.CONNECTED -> startListeningForGestures()
            KtorClientState.PAUSED -> {}
            KtorClientState.DISCONNECTED -> {}
            KtorClientState.ERROR -> {}
        }
    }

    private fun startListeningForGestures() {
        /*CoroutineScope(Dispatchers.IO).launch {
            try {
                ktorClient.gesturesFlow()?.collect { userGesture: UserGesture? ->
                    gesturePlayer.playGesture(userGesture)
                }
            } catch (e: Exception) {
                errorLog(e)
            }
        }*/
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        /*getServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED //or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf("com.android.chrome")
            // flags = AccessibilityServiceInfo.DEFAULT;
            notificationTimeout = 100
        }.also {
            setServiceInfo(it)
        }*/
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

    private fun currentWindowIsChromeWindow(): Boolean {
        return isAppWindow(GOOGLE_CHROME_PACKAGE_NAME)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        event?.also { ev ->
            chromeIsLaunched = currentWindowIsChromeWindow()
            chromeHasContent = currentWindowHasChild()

//            debugLog("windows_check","хром запущен: $chromeIsLaunched, содержимое: $chromeHasContent")

            /*CoroutineScope(Dispatchers.IO).launch {
                if (chromeIsLaunched && chromeHasContent) {
                    if (ktorClient.isNotConnected() && ktorClient.isNotConnectingNow())
                        connectToServer()
                } else {
                    if (ktorClient.isConnected() && ktorClient.isNotDisconnectingNow())
                        disconnectFromServer()
                }
            }*/
        }

        /*if (null != event && event.isWindowStateChanged()) {
            if (isAppWindow(GOOGLE_CHROME_PACKAGE_NAME, true)*//* && trackedWindowNotVisible*//*) {
                debugLog("Гоголь Хром детектед, $dateTimeString")
                trackedWindowHasAppeared.set(true)
                connectToServer()
            } else {
                debugLog("Гоголь Хром ушёл в фон, $dateTimeString")
                trackedWindowHasAppeared.set(false)
                disconnectFromServer()
            }
        }*/

        /*rootInActiveWindow?.also { rootView ->

            val pn = rootView.packageName
            val chCnt = rootView.childCount

            val evTypeString = when(event?.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val et = "WINDOW_STATE_CHANGED"
                    debugLog("$et: package: $pn")
                    et
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
                else -> "Другое событие"
            }

            debugLog("window_state","--------------")
            debugLog("window_state","$evTypeString: package: $pn, child: $chCnt")
//            showChildrenRecursively(0, rootView.getChildren())
        }*/
    }

    /*private fun connectToServer() {

        debugLog("connectToServer()")

        if (null == serverAddress || null == serverPath || serverPort <= 0) {
            errorLog("Неполные настройки сервера: $serverAddress:$serverPort/$serverPath")
            showToast(R.string.gesture_playing_service_error_incomplete_server_config)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            ktorClient.connect(
                serverAddress!!,
                serverPort,
                serverPath!!
            )
        }
    }*/

    private fun disconnectFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            ktorClient.disconnect()
        }
    }

    private fun debugLog(text: String) { Log.d(TAG, text) }
    private fun debugLog(tag: String, text: String) { Log.d(tag, text) }
    private fun errorLog(text: String) { Log.e(TAG, text) }
    private fun errorLog(throwable: Throwable) { Log.e(TAG, ExceptionUtils.getErrorMessage(throwable), throwable) }
    private fun errorLog(text: String, throwable: Throwable) { Log.e(TAG, text, throwable) }

    override fun onInterrupt() {

    }

    private val notificationId: Int = R.id.gesture_playing_service_nitification_id
    private val notificationChannelId: String get() = "${packageName}_notifications"

    companion object {

        val TAG: String = GesturePlayingService::class.java.simpleName
        const val TAG_START_STOP: String = "START_STOP"

        // TODO: вынести в отдельный файл
        const val GOOGLE_CHROME_PACKAGE_NAME = "com.android.chrome"

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
    return when(rootInActiveWindow) {
        null -> false
        else -> rootInActiveWindow.packageName == checkedPackageName
    }
}

fun AccessibilityService.currentWindowHasChild(): Boolean {
    return when(rootInActiveWindow) {
        null -> false
        else -> rootInActiveWindow.childCount > 0
    }
}