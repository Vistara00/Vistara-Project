package com.example.vistaraapp.repositories

import com.example.vistaraapp.api.ApiService  // Import ApiService instead!
import com.example.vistaraapp.api.SessionApi
import com.example.vistaraapp.api_requests_responses.ActiveSessionResponse
import com.example.vistaraapp.api_requests_responses.SosResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class EmergencyRepository(
    private val apiService: ApiService, // Changed back to ApiService
    private val sessionApi: SessionApi
) {

    suspend fun sendSosAlert(token: String, lat: Double, lon: Double, alertType: String = "Distress"): Response<SosResponse> {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val sessionResponse = sessionApi.checkActiveSession(bearerToken)
        val sessionId = if (sessionResponse.isSuccessful) {
            val sessionData = sessionResponse.body()?.data
            sessionData?.sessionId ?: sessionData?.id ?: 0
        } else {
            0
        }

        val requestBody = com.example.vistaraapp.api_requests_responses.SosRequest(
            latitude = lat,
            longitude = lon,
            alertType = alertType,
            sessionId = sessionId
        )
        return apiService.triggerSos(bearerToken, requestBody)
    }

    suspend fun fetchActiveSession(token: String): Result<ActiveSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = sessionApi.checkActiveSession(bearerToken)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Session sync failed: Server code ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun checkIn(token: String, groupSize: Int, vehicleRegistration: String?): Result<ActiveSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val request = com.example.vistaraapp.api_requests_responses.CheckInRequest(groupSize, vehicleRegistration)
                val response = sessionApi.checkIn(bearerToken, request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Check-in failed: Server code ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}