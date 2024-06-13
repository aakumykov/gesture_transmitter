package com.github.aakumykov.client.di.assisted_factories

import android.accessibilityservice.AccessibilityService
import com.github.aakumykov.client.gesture_player.GesturePlayer
import dagger.assisted.AssistedFactory

@AssistedFactory
interface GesturePlayerAssistedFactory{
    fun get(accessibilityService: AccessibilityService): GesturePlayer
}