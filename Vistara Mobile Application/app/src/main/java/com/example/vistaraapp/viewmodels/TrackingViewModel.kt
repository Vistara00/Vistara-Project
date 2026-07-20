package com.example.vistaraapp.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.api.ApiService
import com.example.vistaraapp.api_requests_responses.TrackingUpdateRequest
import kotlinx.coroutines.launch
import android.util.Log

class TrackingViewModel(private val apiService: ApiService) : ViewModel() {

    fun updateLocation(authToken: String, sessionId: Long, lat: Double, lon: Double, accuracy: Double = 10.0, batteryLevel: Int = -1) {
        // viewModelScope is now available because we are inside a ViewModel
        viewModelScope.launch {
            try {
                val request = TrackingUpdateRequest(lat, lon, accuracy, batteryLevel, sessionId)
                val response = apiService.updateTracking("Bearer $authToken", request)

                if (response.isSuccessful) {
                    Log.d("TrackingUpdate", "Success: Location updated")
                } else {
                    Log.e("TrackingUpdate", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                // "e" is now used to log the exception
                Log.e("TrackingUpdate", "Network failure: ${e.localizedMessage}")
            }
        }
    }
}
