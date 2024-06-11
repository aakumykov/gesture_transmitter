package com.github.aakumykov.app_compose

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.aakumykov.app_compose.ui.ClientScreen
import com.github.aakumykov.app_compose.ui.ServerScreen
import com.github.aakumykov.app_compose.ui.WelcomeScreen
import com.github.aakumykov.client.extensions.showToast
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.common.utils.inMainThread
import com.github.aakumykov.prefs_module.PreferencesScreen
import com.github.aakumykov.server.GestureRecorder
import com.github.aakumykov.server.gesture_logger.RoomGestureLogger
import com.github.aakumykov.server.gesture_server.GestureServer
import com.github.aakumykov.server.logDatabase
import com.github.aakumykov.server.log_database.LoggingRepository
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

const val DESTINATION_WELCOME = "DESTINATION_WELCOME"
const val DESTINATION_CLIENT = "DESTINATION_CLIENT"
const val DESTINATION_SERVER = "DESTINATION_SERVER"
const val DESTINATION_SETTINGS = "DESTINATION_SETTINGS"


class ComposeMainActivity : ComponentActivity(), View.OnTouchListener {

    private val gestureRecorder by lazy { GestureRecorder }

    private val gestureServer: GestureServer by lazy {
        GestureServer(
            Gson(),
            RoomGestureLogger(
                LoggingRepository(
                    logDatabase.getLogMessageDAO(),
                    Dispatchers.IO
                )
            ),
            { Date().time }
        )
    }

    private val settingsProvider: SettingsProvider by lazy {
        SettingsProvider.getInstance(App.appContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp),
                color = MaterialTheme.colorScheme.background
            ) {

                NavHost(
                    navController = navController,
                    startDestination = DESTINATION_WELCOME
                ) {
                    composable(DESTINATION_WELCOME) {
                        WelcomeScreen(
                            onClientButtonClicked = { navController.navigate(DESTINATION_CLIENT) },
                            onServerButtonClicked = { navController.navigate(DESTINATION_SERVER) },
                            onSettingsButtonClicked = { navigateToSettings(navController) },
                        )
                    }
                    composable(DESTINATION_CLIENT) {
                        ClientScreen(
                            onSettingsButtonClicked = { navigateToSettings(navController) }
                        )
                    }
                    composable(DESTINATION_SERVER) {
                        ServerScreen(
                            onSettingsButtonClicked = { navigateToSettings(navController) },
                            onTouchListener = this@ComposeMainActivity,
                            settingsProvider = SettingsProvider.getInstance(App.appContext),
                            gestureServer = gestureServer,
                        )
                    }
                    composable(DESTINATION_SETTINGS) {
                        PreferencesScreen(
                            settingsProvider = SettingsProvider.getInstance(App.appContext),
                            onSaveButtonClicked = { navController.popBackStack() },
                            onCancelButtonClicked = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }


    //
    // Обработка жестов на сервере
    //
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> gestureRecorder.startRecording(event)
            MotionEvent.ACTION_MOVE -> gestureRecorder.recordEvent(event)
            MotionEvent.ACTION_UP -> gestureRecorder.finishRecording(event)
            MotionEvent.ACTION_CANCEL -> gestureRecorder.cancelRecording()
            MotionEvent.ACTION_OUTSIDE -> {  }
            else -> {}
        }
        return true
    }

    companion object {
        val TAG: String = ComposeMainActivity::class.java.simpleName
    }
}


fun navigateToSettings(navController: NavController) {
    navController.navigate(DESTINATION_SETTINGS)
    navController.navigate(DESTINATION_SETTINGS)
    navController.navigate(DESTINATION_SETTINGS)
}