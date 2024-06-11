package com.github.aakumykov.app_compose.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.server.gesture_logger.GestureLogReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    gestureLogReader: GestureLogReader,
    onBackButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val dispatcher = Dispatchers.IO

    val content = remember { mutableStateListOf<LogMessage>() }

    LaunchedEffect(Unit) {
        gestureLogReader.getLogMessages().collect { content.add(it) }
    }

    Column(modifier = modifier) {

        SimpleButton(
            text = "Назад",
            onClick = onBackButtonClicked
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(content.size) { index ->
                Text(
                    text = rowText(content[index]),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Divider(color = Color.Gray)
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