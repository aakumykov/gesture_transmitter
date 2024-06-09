package com.github.aakumykov.client.utils

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

class NotificationChannelHelper constructor(private val notificationManagerCompat: NotificationManagerCompat) {

    fun createNotificationChannel(
        id: String,
        importance: Int,
        name: String,
        description: String? = null
    ) {

        val notificationChannelCompat = NotificationChannelCompat.Builder(id, importance).apply {
            setName(name)
            description?.let { setDescription(description) }
        }.build()

        notificationManagerCompat.createNotificationChannel(notificationChannelCompat)
    }


    fun deleteNotificationChannel(channelId: String): Boolean {
        notificationManagerCompat.deleteNotificationChannel(channelId)
        return channelExists(channelId)
    }

    fun channelExists(id: String): Boolean {
        return null != notificationManagerCompat.getNotificationChannel(id)
    }
}