package com.github.aakumykov.app_compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.aakumykov.app_compose.ui.components.WelcomeScreenButton
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.ui.theme.Gesture_transmitterTheme

@Composable
fun WelcomeScreen(
    onClientButtonClicked: () -> Unit,
    onServerButtonClicked: () -> Unit,
    onSettingsButtonClicked: () -> Unit
) {
    Column {
        WelcomeScreenButton(
            textRes = R.string.button_client,
            onClick = onClientButtonClicked
        )

        WelcomeScreenButton(
            textRes = R.string.button_server,
            onClick = onServerButtonClicked
        )
    }
}


@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    Gesture_transmitterTheme {
        WelcomeScreen({},{},{})
    }
}