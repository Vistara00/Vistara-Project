package com.example.vistaraapp.utils

import com.example.vistaraapp.entities_dataclass.NotificationItem

// Helper to identify booking-related notifications
fun isBookingNotification(notification: NotificationItem): Boolean {
    return notification.type.lowercase().contains("booking") ||
           notification.title.lowercase().contains("booking") ||
           notification.message.lowercase().contains("booking")
}

// Helper to identify broadcast-related notifications
fun isBroadcastNotification(notification: NotificationItem): Boolean {
    return notification.type.lowercase().contains("broadcast") ||
           notification.title.lowercase().contains("broadcast") ||
           notification.message.lowercase().contains("broadcast")
}
