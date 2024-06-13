package com.github.aakumykov.app_compose.ui.gui_elements.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.github.aakumykov.app_compose.ui.gui_elements.client.TextInfoView
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.common.utils.NetworkAddressDetector
import kotlinx.coroutines.launch

@Composable
fun IpAddressView(
    messagePrefix: String,
    settingsProvider: SettingsProvider,
    modifier: Modifier = Modifier,
    networkAddressDetector: NetworkAddressDetector? = null
) {

    val scope = rememberCoroutineScope()
    val savedIpAddress = rememberSaveable { mutableStateOf("0.0.0.0") }
    val deviceIpAddress: MutableState<String?> = rememberSaveable { mutableStateOf("0.0.0.0") }

    LaunchedEffect(Unit) {
        scope.launch {
            savedIpAddress.value = settingsProvider.getIpAddress()
            deviceIpAddress.value = networkAddressDetector?.ipAddressInLocalNetwork()
        }
    }

    val deviceIp: String = deviceIpAddress.value?.let { "ip-адрес устройства: ${it}\n" } ?: ""
    val savedIp: String = "${messagePrefix}: ${savedIpAddress.value}"
    val ipInfo = "${deviceIp}${savedIp}"

    TextInfoView(
        text = ipInfo,
        modifier = modifier
    )
}