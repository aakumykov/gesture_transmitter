package com.github.aakumykov.app_compose.ui.gui_elements.client

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.client.client_state_provider.ClientState
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils

@Composable
fun ClientErrorView(clientState: ClientState, e: Exception?, modifier: Modifier = Modifier) {
    if (ClientState.ERROR == clientState && null != e) {
        TextInfoView(
            text = ExceptionUtils.getErrorMessage(e),
            textColorRes = R.color.error,
            modifier = modifier
        )
    }
}