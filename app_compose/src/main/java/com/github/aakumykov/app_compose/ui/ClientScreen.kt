package com.github.aakumykov.app_compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.aakumykov.app_compose.ui.components.SimpleButton

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