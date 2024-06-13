package com.github.aakumykov.client.di.interfaces

import com.github.aakumykov.client.gesture_player.GesturePlayingService

interface GestureClientComponent {
    fun injectToGesturePlayingService(gpService: GesturePlayingService)
}