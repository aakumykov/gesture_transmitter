package com.github.aakumykov.app_compose.ui.gui_elements.client

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.aakumykov.app_compose.ui.screens.clientStateToStringRes
import com.github.aakumykov.client.client_state_provider.ClientState

@Composable
fun ClientState(clientState: ClientState) {
    Text(
        text = stringResource(clientStateToStringRes(clientState)),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}