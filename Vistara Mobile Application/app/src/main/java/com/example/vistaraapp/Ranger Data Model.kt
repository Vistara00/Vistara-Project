package com.example.vistaraapp
//Ranger  responses
data class RangerAlertResponse(
    val success: Boolean,
    val message: String,
    val data: List<RangerAlert>,
    val timestamp: String,
    val statusCode: Int
)
data class RangerAlert(
    val id: Long,
    val sessionId: Long? = null,
    val alertType: String? = null,
    val alertStatus: String? = null,
    val priority: String? = null,
    val visitorName: String? = null,
    val visitorEmail: String? = null,
    val visitorPhone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val message: String,
    val resolutionNotes: String? = null,
    val assignedRangerId: Long? = null,
    val assignedRangerName: String? = null,
    val createdAt: String? = null,
    val respondedAt: String? = null,
    val resolvedAt: String? = null,
    val responseTimeSeconds: Long? = null
)
//stats responses
data class AlertStatisticsResponse(
    val success: Boolean,
    val message: String,
    val data: AlertStatistics,
    val timestamp: String,
    val statusCode: Int
)

data class AlertStatistics(
    val assignedFalseAlarm: Int,
    val totalAssigned: Int,
    val assignedResponding: Int,
    val assignedPending: Int,
    val totalPendingAlerts: Int,
    val assignedResolved: Int
)
//ranger claim
data class ClaimAlertResponse(
    val success: Boolean,
    val message: String,
    val data: ClaimedAlert,
    val timestamp: String,
    val statusCode: Int
)
data class ClaimedAlert(
    val id: Long,
    val sessionId: Long,
    val alertType: String,
    val alertStatus: String,
    val priority: String,
    val visitorName: String,
    val visitorEmail: String,
    val visitorPhone: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val latitude: Double,
    val longitude: Double,
    val message: String,
    val resolutionNotes: String?,
    val assignedRangerId: Long?,
    val assignedRangerName: String?,
    val createdAt: String,
    val respondedAt: String?,
    val resolvedAt: String?,
    val responseTimeSeconds: Long?
)

//ResolvedAlert
data class ResolveAlertResponse(
    val success: Boolean,
    val message: String,
    val data: ResolvedAlert,
    val timestamp: String,
    val statusCode: Int
)
data class ResolvedAlert(
    val id: Long,
    val sessionId: Long,
    val alertType: String,
    val alertStatus: String,
    val priority: String,
    val visitorName: String,
    val visitorEmail: String,
    val visitorPhone: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val latitude: Double,
    val longitude: Double,
    val message: String,
    val resolutionNotes: String?,
    val assignedRangerId: Long?,
    val assignedRangerName: String?,
    val createdAt: String,
    val respondedAt: String?,
    val resolvedAt: String?,
    val responseTimeSeconds: Long?
)

data class ResolveAlertRequest(
    val notes: String?
)

// QR Check-in response models
data class QrCheckInResponseData(
    val bookingId: Long?,
    val visitorName: String?,
    val bookingReference: String?,
    val status: String?
)

data class QrCheckInResponse(
    val success: Boolean,
    val message: String,
    val data: QrCheckInResponseData?,
    val timestamp: String?,
    val statusCode: Int?
)