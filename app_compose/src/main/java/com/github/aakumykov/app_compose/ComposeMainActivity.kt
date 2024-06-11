package com.github.aakumykov.app_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.aakumykov.app_compose.ui.theme.Gesture_transmitterTheme

const val DESTINATION_WELCOME = "DESTINATION_WELCOME"
const val DESTINATION_CLIENT = "DESTINATION_CLIENT"
const val DESTINATION_SERVER = "DESTINATION_SERVER"


class ComposeMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = DESTINATION_WELCOME
            ) {
                composable(DESTINATION_WELCOME) {
                    WelcomeScreen(
                        { navController.navigate(DESTINATION_CLIENT) },
                        { navController.navigate(DESTINATION_SERVER) }
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
            }
        }
    }
}


@Composable
fun ClientScreen(onBackButtonClick: () -> Unit) {
    Text(text = "Клиент")
}

@Composable
fun ServerScreen(onBackButtonClick: () -> Unit) {
    Text(text = "Сервер")
}


@Composable
fun WelcomeScreen(
    onClientButtonClicked: () -> Unit,
    onServerButtonClicked: () -> Unit
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
            .padding(all = 12.dp),
        onClick = { onClick.invoke() }
    ) {
        Text(text = stringResource(id = textRes))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Gesture_transmitterTheme {
        WelcomeScreen({},{})
    }
}