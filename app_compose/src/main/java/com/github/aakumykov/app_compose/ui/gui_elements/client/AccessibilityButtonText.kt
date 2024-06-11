package com.github.aakumykov.app_compose.ui.gui_elements.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.github.aakumykov.client.R

@Composable
fun accessibilityButtonText(accButtonText: MutableState<Boolean>): String {
    return if (accButtonText.value) stringResource(R.string.button_acc_service_enabled)
    else stringResource(R.string.button_acc_service_disabled)
}