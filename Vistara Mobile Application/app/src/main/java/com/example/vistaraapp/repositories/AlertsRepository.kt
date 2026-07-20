package com.example.vistaraapp.repositories

import com.example.vistaraapp.AlertStatisticsResponse
import com.example.vistaraapp.RangerAlertResponse
import com.example.vistaraapp.ClaimAlertResponse
import com.example.vistaraapp.ResolveAlertResponse
import com.example.vistaraapp.ResolveAlertRequest
import com.example.vistaraapp.api.ApiService
import com.example.vistaraapp.api_requests_responses.AlertsGeneralDto
import retrofit2.Response

class AlertsRepository(private val apiService: ApiService) {

    suspend fun getAllAlerts(token: String): Response<AlertsGeneralDto> {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.getAllAlerts(bearer)
    }

    suspend fun getAssignedAlerts(token: String): Response<RangerAlertResponse> {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.getAssignedAlerts(bearer)
    }

    suspend fun getAlertsByStatus(token: String, status: String): Response<RangerAlertResponse> {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.getAlertsByStatus(bearer, status)
    }

    suspend fun getRangerStats(token: String): Response<AlertStatisticsResponse> {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.noOfSOS(bearer)
    }

    suspend fun assignToMe(token: String, alertId: Long): ClaimAlertResponse {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.assignToMe(bearer, alertId)
    }

    suspend fun resolveAlert(token: String, alertId: Long, notes: String?): ResolveAlertResponse {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.resolveAlert(bearer, alertId, ResolveAlertRequest(notes))
    }

    suspend fun getPendingEmergencies(token: String): Response<RangerAlertResponse> {
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.getPendingEmergencies(bearer)
    }
}
