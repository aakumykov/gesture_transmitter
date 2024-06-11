package com.github.aakumykov.app_compose.ui.gui_elements.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
fun TextPlaceholder(stringRes: Int) {
    Text(
        text = stringResource(stringRes),
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )
}