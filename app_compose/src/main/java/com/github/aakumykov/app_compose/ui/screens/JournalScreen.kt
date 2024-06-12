package com.github.aakumykov.app_compose.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.server.gesture_logger.GestureLogReader

@Composable
fun JournalScreen(
    gestureLogReader: GestureLogReader,
    onBackButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val LOG_TAG = "JournalScreen"
    val content = remember { mutableStateListOf<LogMessage>() }

    LaunchedEffect(Unit) {
        gestureLogReader.getLogMessagesAsList().also {
            content.addAll(it)
        }
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
fun LogLines(
    content: SnapshotStateList<LogMessage>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(content.size) { index ->
            val logMessage: LogMessage? = if (content.size > index) content[index] else null
            logMessage?.also { logMessage ->
                Text(
                    text = logMessage.toString(),
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
