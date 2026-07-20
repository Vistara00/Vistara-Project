package com.example.vistaraapp.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.EmergencyTrackingService
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.api_requests_responses.SosRequest
import com.example.vistaraapp.utils.TokenManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class SosViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var sosMessage by mutableStateOf<String?>(null)
        private set

    @SuppressLint("MissingPermission")
    fun triggerEmergencySos(context: Context, alertType: String, message: String? = null) {
        isLoading = true
        sosMessage = "Verifying security session..."

        val savedToken = TokenManager.getToken()
        if (savedToken.isNullOrBlank()) {
            isLoading = false
            sosMessage = "Authentication failed."
            return
        }

        val formattedBearerToken = if (savedToken.startsWith("Bearer ")) savedToken else "Bearer $savedToken"
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                sendSosToBackend(context, formattedBearerToken, location.latitude, location.longitude, alertType, message ?: "Emergency SOS Triggered")
            } else {
                isLoading = false
                sosMessage = "GPS lock failed."
            }
        }.addOnFailureListener {
            isLoading = false
            sosMessage = "Hardware failure: ${it.localizedMessage}"
        }
    }

    private fun sendSosToBackend(context: Context, bearerToken: String, lat: Double, lon: Double, type: String, msg: String) {
        sosMessage = "Transmitting distress signal..."

        viewModelScope.launch {
            try {
                val sessionResponse = RetrofitClient.sessionInstance.checkActiveSession(bearerToken)
                val sessionId = if (sessionResponse.isSuccessful) {
                    sessionResponse.body()?.data?.sessionId ?: sessionResponse.body()?.data?.id ?: 0
                } else 0

                val payload = SosRequest(lat, lon, type, sessionId, msg)
                val response = RetrofitClient.bookingInstance.triggerSos(bearerToken, payload)

                if (response.isSuccessful && response.body()?.success == true) {
                    sosMessage = "SOS Broadcast Successful! Help is on the way."

                    // START THE FOREGROUND SERVICE
                    val intent = Intent(context, EmergencyTrackingService::class.java).apply {
                        putExtra("AUTH_TOKEN", bearerToken.removePrefix("Bearer ").trim())
                        putExtra("SESSION_ID", sessionId.toLong())
                    }
                    context.startForegroundService(intent)
                } else {
                    sosMessage = "Alert Rejected: ${response.message()}"
                }
            } catch (e: Exception) {
                sosMessage = "Network Failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}