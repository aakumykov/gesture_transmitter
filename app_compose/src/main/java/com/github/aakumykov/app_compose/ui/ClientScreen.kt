package com.github.aakumykov.app_compose.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.ui.components.SimpleButton
import com.github.aakumykov.client.extensions.showToast
import com.github.aakumykov.common.config.GOOGLE_CHROME_PACKAGE_NAME
import com.github.aakumykov.common.extentions.showToast

@Composable
fun ClientScreen(onSettingsButtonClicked: () -> Unit) {

    val context = LocalContext.current

    Column {

        SimpleButton(
            text = stringResource(id = com.github.aakumykov.client.R.string.button_acc_service_disabled),
            bgColor = Color.Blue,
        ) {

        }

        SimpleButton(
            text = "Настройки",
            bgColor = Color.Blue,
            onClick = onSettingsButtonClicked
        )

        SimpleButton(
            text = "Запустить Google Chrome",
            bgColor = Color.Blue,
            onClick = {
                launchGoogleChrome(context)
            }
        )
    }
}

private fun launchGoogleChrome(context: Context) {
    context.packageManager
        .getLaunchIntentForPackage(GOOGLE_CHROME_PACKAGE_NAME)
        ?.also {
            startActivity(context, it, null)
        }
        ?: showToast(context, com.github.aakumykov.client.R.string.google_chrome_not_found)
}

fun showToast(context: Context, stringRes: Int) {
    context.showToast(stringRes)
}