package com.github.aakumykov.app_compose.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ServerScreen(onSettingsButtonClicked: () -> Unit) {
    Text(text = "Сервер")
}