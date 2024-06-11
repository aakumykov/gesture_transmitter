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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.aakumykov.app_compose.ui.ClientScreen
import com.github.aakumykov.app_compose.ui.ServerScreen
import com.github.aakumykov.app_compose.ui.WelcomeScreen
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.prefs_module.PreferencesScreen

const val DESTINATION_WELCOME = "DESTINATION_WELCOME"
const val DESTINATION_CLIENT = "DESTINATION_CLIENT"
const val DESTINATION_SERVER = "DESTINATION_SERVER"
const val DESTINATION_SETTINGS = "DESTINATION_SETTINGS"


class ComposeMainActivity : ComponentActivity() {
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
                            onSettingsButtonClicked = { navController.navigate(DESTINATION_SETTINGS) },
                        )
                    }
                    composable(DESTINATION_CLIENT) {
                        ClientScreen {
                            navController.popBackStack()
                        }
                    }
                    composable(DESTINATION_SERVER) {
                        ServerScreen {
                            navController.popBackStack()
                        }
                    }
                    composable(DESTINATION_SETTINGS) {
                        PreferencesScreen(
                            settingsProvider = SettingsProvider.getInstance(App.appContext),
                            onSaveButtonClicked = {},
                            onCancelButtonClicked = { navController.popBackStack() }
                        )
                    }
                }

            }
        }
    }
}