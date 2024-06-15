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
    isServer: Boolean,
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

    val deviceIp: String = "ip-адрес устройства: " + (deviceIpAddress.value ?: "НЕ ОПРЕДЕЛЁН")
    val savedIp = "сохранённый ip-адрес: ${savedIpAddress.value}"

    var ipInfo = savedIp

    if (isServer)
        ipInfo = "${deviceIp}\n${savedIp}"

    TextInfoView(
        text = ipInfo,
        modifier = modifier
    )
}