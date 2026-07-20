package com.example.vistaraapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.api_requests_responses.AlertItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeneralRangerAlertsViewModel(
    private val repository: RangerGeneralAlertsRepository
) : ViewModel() {

    private val _alertsList = MutableStateFlow<List<AlertItemDto>>(emptyList())
    val alertsList: StateFlow<List<AlertItemDto>> = _alertsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchAlerts(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getAllAlerts(token)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _alertsList.value = body.data ?: emptyList()
                    } else {
                        _errorMessage.value = body.message ?: "Failed to load alerts"
                        Log.e("ALERT_DEBUG", "API returned success=false: ${body.message}")
                    }
                } else {
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("ALERT_DEBUG", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                Log.e("ALERT_DEBUG", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}