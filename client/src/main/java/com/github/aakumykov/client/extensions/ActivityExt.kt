package com.github.aakumykov.client.extensions

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.client.R

/**
 * @param accessibilityServiceId Строка вида packageName/.AccessibilityServiceClassName
 * Пример: com.github.aakumykov.kotlin_playground/.MyAccessibilityService
 */
fun Activity.isAccessibilityServiceEnabled(accessibilityServiceId: String): Boolean {
    return (getSystemService(AppCompatActivity.ACCESSIBILITY_SERVICE) as AccessibilityManager)
        .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
        .any { accessibilityServiceInfo ->
            accessibilityServiceInfo.id.equals(accessibilityServiceId)
        }
}

fun Activity.openAccessibilitySettings() {
    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}