package com.example.vistaraapp.api_requests_responses

import com.example.vistaraapp.entities_dataclass.NotificationItem
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class NotificationData(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("read") val read: Boolean,
    @SerializedName("broadcast") val broadcast: Boolean,
    @SerializedName("referenceId") val referenceId: Int?,
    @SerializedName("createdAt") val createdAt: String
)

data class NotificationListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<NotificationData>,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("statusCode") val statusCode: Int

)
data class UnreadCountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Int, //passes the integer count inside data
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("statusCode") val statusCode: Int
)

data class NotificationReadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("statusCode") val statusCode: Int? = null
)


// The mapper function lives directly inside this file now!
fun NotificationData.toUiModel(): NotificationItem {
    return NotificationItem(
        id = this.id.toString(),
        title = this.title,
        message = this.message, // Maps to your layout's 'message' property
        timestamp = formatServerTimestamp(this.createdAt),
        type = this.type,
        isAlert = this.type == "BROADCAST" || this.type == "WEATHER_ALERT" || this.type == "EMERGENCY_ALERT"
    )
}

private fun formatServerTimestamp(rawTimestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(rawTimestamp) ?: return "Just now"
        val diff = Date().time - date.time

        when {
            diff < 0 -> "Just now"
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} mins ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 172800_000 -> "Yesterday"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) {
        "Recently"
    }
}