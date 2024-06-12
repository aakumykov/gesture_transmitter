package com.github.aakumykov.app_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.aakumykov.app_compose.ui.screens.ClientScreen
import com.github.aakumykov.app_compose.ui.screens.JournalScreen
import com.github.aakumykov.app_compose.ui.screens.ServerScreen
import com.github.aakumykov.app_compose.ui.screens.WelcomeScreen
import com.github.aakumykov.client.client_state_provider.KtorStateProvider
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.settings_provider.SettingsProvider
import com.github.aakumykov.common.utils.TimestampSupplier
import com.github.aakumykov.app_compose.ui.screens.SettingsScreen
import com.github.aakumykov.server.GestureRecorder
import com.github.aakumykov.logger.gesture_logger.RoomGestureLogger
import com.github.aakumykov.server.GestureServer
import com.github.aakumykov.logger.gesture_logger.GestureLogReader
import com.github.aakumykov.logger.loggingDatabase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

const val DESTINATION_WELCOME = "DESTINATION_WELCOME"
const val DESTINATION_CLIENT = "DESTINATION_CLIENT"
const val DESTINATION_SERVER = "DESTINATION_SERVER"
const val DESTINATION_SETTINGS = "DESTINATION_SETTINGS"
const val DESTINATION_JOURNAL = "DESTINATION_JOURNAL"


class ComposeMainActivity : ComponentActivity() {

    //
    // Монструозное ручное внедрение зависимостей, так как перейти на Dagger2 я не успел.
    // Демонстрация владения технологией здесь: https://github.com/aakumykov/cloud_sync/tree/master/app/src/main/java/com/github/aakumykov/sync_dir_to_cloud/di
    //

    @Inject
    private lateinit var gestureRecorder: GestureRecorder

    private val ioDispatcher by lazy { Dispatchers.IO }

    @Inject
    protected lateinit var roomGestureLogger: RoomGestureLogger

    @Inject
    protected lateinit var gestureLogReader: GestureLogReader

    @Inject
    protected lateinit var settingsProvider: SettingsProvider

    @Inject
    private lateinit var gestureServer: GestureServer

    @Inject
    private lateinit var gestureClient: GestureClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        App.appComponent.injectToComposeMainActivity(this)

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
                    //
                    // Начальный экран
                    //
                    composable(DESTINATION_WELCOME) {
                        WelcomeScreen(
                            onClientButtonClicked = { navController.navigate(DESTINATION_CLIENT) },
                            onServerButtonClicked = { navController.navigate(DESTINATION_SERVER) },
                            onSettingsButtonClicked = { navigateToSettings(navController) },
                        )
                    }

                    //
                    // Клиент
                    //
                    composable(DESTINATION_CLIENT) {
                        ClientScreen(
                            gestureClient = gestureClient,
                            settingsProvider = settingsProvider,
                            coroutineDispatcher = ioDispatcher,
                            onSettingsButtonClicked = { navigateToSettings(navController) }
                        )
                    }

                    //
                    // Сервер
                    //
                    composable(DESTINATION_SERVER) {
                        ServerScreen(
                            onSettingsButtonClicked = { navigateToSettings(navController) },
                            onJournalButtonClicked = { navController.navigate(DESTINATION_JOURNAL) },
                            settingsProvider = settingsProvider,
                            gestureServer = gestureServer,
                            gestureRecorder = gestureRecorder
                        )
                    }

                    //
                    // Настройки
                    //
                    composable(DESTINATION_SETTINGS) {
                        SettingsScreen(
                            settingsProvider = settingsProvider,
                            onSaveButtonClicked = { navController.popBackStack() },
                            onCancelButtonClicked = { navController.popBackStack() }
                        )
                    }

                    //
                    // Журнал
                    //
                    composable(DESTINATION_JOURNAL) {
                        JournalScreen(
                            gestureLogReader = gestureLogReader,
                            onBackButtonClicked = { navController.popBackStack() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    private fun navigateToSettings(navController: NavController) {
        navController.navigate(DESTINATION_SETTINGS)
    }
}


