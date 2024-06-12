package com.github.aakumykov.app_compose.ui.gui_elements.client

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.aakumykov.app_compose.funstions.ui.clientStateToStringRes
import com.github.aakumykov.client.client_state_provider.ClientState

@Composable
fun ClientStateView(clientState: ClientState, modifier: Modifier = Modifier) {
    TextInfoView(
        text = stringResource(clientStateToStringRes(clientState)),
        modifier = modifier
    )
}