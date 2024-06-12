package com.github.aakumykov.app_compose.ui.gui_elements.client

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.funstions.ui.clientStateToStringRes
import com.github.aakumykov.client.client_state_provider.ClientState

@Composable
fun TextInfoView(
    text: String,
    modifier: Modifier = Modifier,
    textColorRes: Int = R.color.info_view_default
) {
    Text(
        text = text,
        color = colorResource(textColorRes),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}