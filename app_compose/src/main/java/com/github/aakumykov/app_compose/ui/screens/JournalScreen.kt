package com.github.aakumykov.app_compose.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.ui.gui_elements.client.TextInfoView
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.server.gesture_logger.GestureLogReader
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun JournalScreen(
    gestureLogReader: GestureLogReader,
    onBackButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content = remember { mutableStateListOf<LogMessage>() }

    LaunchedEffect(Unit) {
        gestureLogReader.getLogMessages().collect { content.add(it) }
    }

    Column(modifier = modifier) {

        SimpleButton(
            text = "Назад",
            onClick = onBackButtonClicked
        )

        LogLines(content)
    }
}

@Composable
fun LogLines(content: SnapshotStateList<LogMessage>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(content.size) { index ->
            val logMessage: LogMessage? = if (content.size > index) content[index] else null
            logMessage?.also {
                Text(
                    text = rowText(it),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Divider(color = colorResource(R.color.log_divider))
            }
        }
    }
}

fun rowText(logMessage: LogMessage): String {
    return "${timestampToTime(logMessage.timestamp)}, ${logMessage.message}"
}

@SuppressLint("SimpleDateFormat")
fun timestampToTime(ts: Long): String {
    return SimpleDateFormat("dd.MM.LL hh:mm:ss")
        .format(Date(ts))
}