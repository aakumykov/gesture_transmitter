package com.github.aakumykov.app_compose.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.funstions.keyboard.decimalKeyboardOptions
import com.github.aakumykov.app_compose.ui.gui_elements.settings.InputField
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.app_compose.ui.theme.Gesture_transmitterTheme
import com.github.aakumykov.common.settings_provider.SettingsProvider

@Composable
fun SettingsScreen(
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
        SimpleButton(
            text = "Сохранить",
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            onClick = {
                settingsProvider.storeIpAddress(ipAddress.value)
                settingsProvider.storePort(port.intValue)
                settingsProvider.storePath(path.value)
                onSaveButtonClicked.invoke()
            }
        )

        //
        // Отменить
        //
        SimpleButton(
            text = "Отменить",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            bgColor = colorResource(id = R.color.button_cancel),
            onClick = onCancelButtonClicked
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    Gesture_transmitterTheme {
        SettingsScreen(
            settingsProvider = SettingsProvider.getInstance(LocalContext.current),
            onSaveButtonClicked = {},
            onCancelButtonClicked = {}
        )
    }
}