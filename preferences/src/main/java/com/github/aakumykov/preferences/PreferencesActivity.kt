package com.github.aakumykov.preferences

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.aakumykov.common.R
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.preferences.ui.theme.Gesture_transmitterTheme

class PreferencesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Gesture_transmitterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PreferencesScreen(
                        SettingsProvider.getInstance(this),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PreferencesScreen(settingsProvider: SettingsProvider, modifier: Modifier = Modifier) {

    val ipAddress = remember {
        mutableStateOf(settingsProvider.getIpAddress())
    }

    Column(modifier = modifier.fillMaxSize()) {

        OutlinedTextField(
            value = ipAddress.value.toString(),
            onValueChange = {
                settingsProvider.storeIpAddress(ipAddress.value.toString())
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = settingsProvider.getPort().toString(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = settingsProvider.getPath().toString(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Сохранить")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.button_cancel)
            ),
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Отмена")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Gesture_transmitterTheme {
        PreferencesScreen(SettingsProvider.getInstance(LocalContext.current))
    }
}