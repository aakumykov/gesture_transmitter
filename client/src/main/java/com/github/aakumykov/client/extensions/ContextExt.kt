package com.github.aakumykov.client.extensions

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.startActivity

fun Context.isAccessibilityServiceEnabled(serviceClass: Class<out AccessibilityService>): Boolean
{
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

    for (enabledService in enabledServices) {
        val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
        if (enabledServiceInfo.packageName.equals(packageName)
            && enabledServiceInfo.name.equals(serviceClass.name)) return true
    }

    return false
}


fun Context.openAccessibilitySettings() {
    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}


fun Context.showToast(@StringRes stringRes: Int) {
    showToast(getString(stringRes))
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}