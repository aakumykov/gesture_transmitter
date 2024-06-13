package com.github.aakumykov.app_compose.ui.gui_elements.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.github.aakumykov.app_compose.ui.gui_elements.client.TextInfoView
import com.github.aakumykov.common.settings_provider.SettingsProvider
import kotlinx.coroutines.launch

@Composable
fun IpAddressView(
    messagePrefix: String,
    settingsProvider: SettingsProvider,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()
    val ipAddress = rememberSaveable { mutableStateOf("0.0.0.0") }

    LaunchedEffect(Unit) {
        scope.launch {
            ipAddress.value = settingsProvider.getIpAddress()
        }
    }

    TextInfoView(
        text = messagePrefix + ": " + ipAddress.value,
        modifier = modifier
    )
}