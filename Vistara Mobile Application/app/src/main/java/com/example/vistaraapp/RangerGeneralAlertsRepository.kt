package com.example.vistaraapp

import com.example.vistaraapp.api.ApiService
import com.example.vistaraapp.api_requests_responses.AlertsGeneralDto
import retrofit2.Response

class RangerGeneralAlertsRepository(private val apiService: ApiService) {
    suspend fun getAllAlerts(token: String): Response<AlertsGeneralDto> {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return apiService.getAllAlerts(bearerToken)
    }
}