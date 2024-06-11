package com.github.aakumykov.app_compose.funstions.keyboard

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

fun textKeyboardOptions(): KeyboardOptions {
    return KeyboardOptions(
        keyboardType = KeyboardType.Text,
        autoCorrect = false,
    )
}

fun decimalKeyboardOptions(): KeyboardOptions {
    return KeyboardOptions(
        keyboardType = KeyboardType.Decimal,
        autoCorrect = false,
    )
}