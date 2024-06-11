package com.github.aakumykov.app_compose.ui.gui_elements.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.aakumykov.app_compose.funstions.keyboard.textKeyboardOptions

@Composable
fun InputField(text: String,
               placeholderRes: Int,
               keyboardOptions: KeyboardOptions = textKeyboardOptions(),
               enabled: Boolean = true,
               onValueChange: (String) -> Unit) {
    OutlinedTextField(
        enabled = enabled,
        value = text,
        placeholder = { TextPlaceholder(stringRes = placeholderRes) },
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp)
    )
}