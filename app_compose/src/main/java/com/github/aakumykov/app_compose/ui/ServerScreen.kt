package com.github.aakumykov.app_compose.ui

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.viewinterop.AndroidView
import com.github.aakumykov.app_compose.ui.components.SimpleButton

@Composable
fun ServerScreen(
    onSettingsButtonClicked: () -> Unit,
    onStartServerButtonClicked: () -> Unit,
    onStopServerButtonClicked: () -> Unit,
) {

    Column {

        SimpleButton(
            text = "Настройки",
            bgColor = Color.Blue,
            onClick = onSettingsButtonClicked
        )

        SimpleButton(
            text = "Запустить сервер",
            bgColor = Color.Cyan,
            onClick = onStartServerButtonClicked
        )

        SimpleButton(
            text = "Остановить сервер",
            bgColor = Color.DarkGray,
            onClick = onStopServerButtonClicked
        )

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp)
                .heightIn(min = 100.dp, max = 300.dp)
                .background(color = colorResource(com.github.aakumykov.server.R.color.touch_recording_area)),
            factory = { context ->
                TextView(context).apply {
                    gravity = Gravity.CENTER
                    text = "Запустите сервер.\n" +
                            "Подключите клиента." +
                            "Водите пальцем вверх-вниз"
                }
            },
            update = {

            }
        )
    }

}