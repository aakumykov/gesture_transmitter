package com.github.aakumykov.prefs_module

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.prefs_module.theme.Gesture_transmitterTheme

@Composable
fun PreferencesScreen(
    settingsProvider: SettingsProvider,
    modifier: Modifier = Modifier,
    onSaveButtonClicked: () -> Unit,
    onCancelButtonClicked: () -> Unit
) {

    val ipAddress = rememberSaveable {
        mutableStateOf(settingsProvider.getIpAddress())
    }

    val port = rememberSaveable {
        mutableIntStateOf(settingsProvider.getPort())
    }

    val path = rememberSaveable {
        mutableStateOf(settingsProvider.getPath())
    }

    val activity = (LocalContext.current as? Activity)


    Column(modifier = modifier.fillMaxSize()) {

        //
        // Адрес
        //
        InputField(
            text = ipAddress.value,
            placeholderRes = R.string.server_address_placeholder,
            keyboardOptions = decimalKeyboardOptions()
        ) {
            ipAddress.value = it
        }

        //
        // Порт
        //
        InputField(
            text = port.intValue.toString(),
            placeholderRes = R.string.server_port_placeholder,
            keyboardOptions = decimalKeyboardOptions()
        ) {
            port.intValue = it.toInt()
        }

        //
        // Путь
        //
        InputField(
            text = path.value,
            placeholderRes = R.string.server_path_placeholder
        ) {
            path.value = it
        }

        //
        // Сохранить
        //
        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            onClick = {
                settingsProvider.storeIpAddress(ipAddress.value)
                settingsProvider.storePort(port.intValue)
                settingsProvider.storePath(path.value)
                onSaveButtonClicked.invoke()
            }
        ) {
            Text("Сохранить")
        }

        //
        // Отменить
        //
        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.button_cancel)
            ),
            onClick = onCancelButtonClicked
        ) {
            Text("Отмена")
        }
    }
}

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


@Composable
fun TextPlaceholder(stringRes: Int) {
    Text(
        text = stringResource(stringRes),
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )
}


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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Gesture_transmitterTheme {
        PreferencesScreen(
            settingsProvider = SettingsProvider.getInstance(LocalContext.current),
            onSaveButtonClicked = {},
            onCancelButtonClicked = {}
        )
    }
}