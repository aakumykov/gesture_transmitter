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
import com.github.aakumykov.app_compose.ui.screens.SettingsScreen
import com.github.aakumykov.app_compose.ui.screens.WelcomeScreen
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.di.annotations.IODispatcher
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.common.utils.NetworkAddressDetector
import com.github.aakumykov.logger.gesture_logger.GestureLogReader
import com.github.aakumykov.logger.gesture_logger.RoomGestureLogger
import com.github.aakumykov.server.GestureRecorder
import com.github.aakumykov.server.GestureServer
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

const val DESTINATION_WELCOME = "DESTINATION_WELCOME"
const val DESTINATION_CLIENT = "DESTINATION_CLIENT"
const val DESTINATION_SERVER = "DESTINATION_SERVER"
const val DESTINATION_SETTINGS = "DESTINATION_SETTINGS"
const val DESTINATION_JOURNAL = "DESTINATION_JOURNAL"


class ComposeMainActivity : ComponentActivity() {

    @Inject
    protected lateinit var gestureRecorder: GestureRecorder

    @Inject
    @IODispatcher
    protected lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    protected lateinit var roomGestureLogger: RoomGestureLogger

    @Inject
    protected lateinit var gestureLogReader: GestureLogReader

    @Inject
    protected lateinit var settingsProvider: SettingsProvider

    @Inject
    protected lateinit var networkAddressDetector: NetworkAddressDetector

    @Inject
    protected lateinit var gestureServer: GestureServer

    @Inject
    protected lateinit var gestureClient: GestureClient



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
                            networkAddressDetector = networkAddressDetector,
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


