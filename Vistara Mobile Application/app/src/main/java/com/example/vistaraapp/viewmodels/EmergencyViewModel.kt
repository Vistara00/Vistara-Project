package com.example.vistaraapp.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.repositories.EmergencyRepository
import kotlinx.coroutines.launch

class EmergencyViewModel(private val repository: EmergencyRepository) : ViewModel() {

    private val _sosStatus = mutableStateOf<String?>(null)
    val sosStatus: State<String?> = _sosStatus

    fun triggerSosAlert(jwtToken: String, latitude: Double? = null, longitude: Double? = null, alertType: String = "Distress") {
        viewModelScope.launch {
            _sosStatus.value = "SENDING"
            try {
                val formattedToken = if (jwtToken.startsWith("Bearer ")) jwtToken else "Bearer $jwtToken"
                val lat = latitude ?: 0.0
                val lon = longitude ?: 0.0
                val response = repository.sendSosAlert(formattedToken, lat, lon, alertType)

                if (response.isSuccessful && response.body() != null) {
                    _sosStatus.value = "SUCCESS: ${response.body()?.message}"
                } else {
                    _sosStatus.value = "FAILED: Error Code ${response.code()}"
                }
            } catch (e: Exception) {
                _sosStatus.value = "ERROR: ${e.localizedMessage}"
            }
        }
    }

    fun resetSosStatus() {
        _sosStatus.value = null
    }
}