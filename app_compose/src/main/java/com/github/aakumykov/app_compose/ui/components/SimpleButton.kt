package com.github.aakumykov.app_compose.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SimpleButton(text: String, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor)
    ) {
        Text(text = text)
    }
}