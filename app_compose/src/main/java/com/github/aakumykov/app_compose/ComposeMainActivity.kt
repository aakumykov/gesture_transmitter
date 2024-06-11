package com.github.aakumykov.app_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.aakumykov.app_compose.ui.theme.Gesture_transmitterTheme
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
                            settingsProvider = SettingsProvider.getInstance(AppCompose.appContext),
                            onSaveButtonClicked = {},
                            onCancelButtonClicked = { navController.popBackStack() }
                        )
                    }
                }

            }
        }
    }
}



@Composable
fun ClientScreen(onSettingsButtonClicked: () -> Unit) {
    Column {
        SimpleButton(
            text = "Настройки",
            bgColor = Color.Blue,
            onClick = onSettingsButtonClicked
        )
    }
}


@Composable
fun SimpleButton(text: String, bgColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor)
    ) {
        Text(text = text)
    }
}

@Composable
fun ServerScreen(onSettingsButtonClicked: () -> Unit) {
    Text(text = "Сервер")
}


@Composable
fun WelcomeScreen(
    onClientButtonClicked: () -> Unit,
    onServerButtonClicked: () -> Unit,
    onSettingsButtonClicked: () -> Unit
) {
    Column {
        MainButton(
            textRes = R.string.button_client,
            onClick = onClientButtonClicked
        )

        MainButton(
            textRes = R.string.button_server,
            onClick = onServerButtonClicked
        )

        MainButton(
            textRes = R.string.button_settings,
            onClick = onSettingsButtonClicked
        )
    }
}


@Composable
fun MainButton(
    textRes: Int,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        onClick = { onClick.invoke() }
    ) {
        Text(text = stringResource(id = textRes))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Gesture_transmitterTheme {
        WelcomeScreen({},{},{})
    }
}