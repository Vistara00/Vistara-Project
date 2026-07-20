package com.example.vistaraapp.entities_dataclass

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: String,
    val isAlert: Boolean = false
)
