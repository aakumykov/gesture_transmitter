package com.github.aakumykov.app_compose

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.aakumykov.app_compose.ui.ClientScreen
import com.github.aakumykov.app_compose.ui.ServerScreen
import com.github.aakumykov.app_compose.ui.WelcomeScreen
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.prefs_module.PreferencesScreen
import com.github.aakumykov.server.GestureRecorder
import com.github.aakumykov.server.gesture_logger.RoomGestureLogger
import com.github.aakumykov.server.gesture_server.GestureServer
import com.github.aakumykov.server.logDatabase
import com.github.aakumykov.server.log_database.LoggingRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import java.util.Date

const val DESTINATION_WELCOME = "DESTINATION_WELCOME"
const val DESTINATION_CLIENT = "DESTINATION_CLIENT"
const val DESTINATION_SERVER = "DESTINATION_SERVER"
const val DESTINATION_SETTINGS = "DESTINATION_SETTINGS"


class ComposeMainActivity : ComponentActivity() {

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
                            settingsProvider = settingsProvider,
                            gestureServer = gestureServer,
                            gestureRecorder = gestureRecorder
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
}


fun navigateToSettings(navController: NavController) {
    navController.navigate(DESTINATION_SETTINGS)
    navController.navigate(DESTINATION_SETTINGS)
    navController.navigate(DESTINATION_SETTINGS)
}