package com.example.vistaraapp.api_requests_responses

import com.example.vistaraapp.RangerAlert
import com.google.gson.annotations.SerializedName

data class AlertsGeneralDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<AlertItemDto>?,
    @SerializedName("statusCode") val statusCode: Int?
)

typealias AlertResponseDto = AlertsGeneralDto

data class AlertItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("sessionId") val sessionId: Long? = null,
    @SerializedName("alertType") val alertType: String? = null,
    @SerializedName("alertStatus") val alertStatus: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("visitorName") val visitorName: String? = null,
    @SerializedName("visitorEmail") val visitorEmail: String? = null,
    @SerializedName("visitorPhone") val visitorPhone: String? = null,
    @SerializedName("emergencyContactName") val emergencyContactName: String? = null,
    @SerializedName("emergencyContactPhone") val emergencyContactPhone: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("resolutionNotes") val resolutionNotes: String? = null,
    @SerializedName("assignedRangerId") val assignedRangerId: Long? = null,
    @SerializedName("assignedRangerName") val assignedRangerName: String? = null,
    @SerializedName("assignedRanger") val assignedRanger: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("respondedAt") val respondedAt: String? = null,
    @SerializedName("resolvedAt") val resolvedAt: String? = null,
    @SerializedName("responseTimeSeconds") val responseTimeSeconds: Long? = null
) {
    fun toRangerAlert(): RangerAlert {
        return RangerAlert(
            id = this.id,
            sessionId = this.sessionId,
            alertType = this.alertType,
            alertStatus = this.alertStatus,
            priority = this.priority,
            visitorName = this.visitorName,
            visitorEmail = this.visitorEmail,
            visitorPhone = this.visitorPhone,
            emergencyContactName = this.emergencyContactName,
            emergencyContactPhone = this.emergencyContactPhone,
            latitude = this.latitude,
            longitude = this.longitude,
            message = this.message ?: "",
            resolutionNotes = this.resolutionNotes,
            assignedRangerId = this.assignedRangerId,
            assignedRangerName = this.assignedRangerName ?: this.assignedRanger,
            createdAt = this.createdAt,
            respondedAt = this.respondedAt,
            resolvedAt = this.resolvedAt,
            responseTimeSeconds = this.responseTimeSeconds
        )
    }
}