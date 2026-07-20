package com.example.vistaraapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.api_requests_responses.ActiveSessionResponse
import com.example.vistaraapp.repositories.EmergencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Sealed interface for UI State
sealed interface SessionUiState {
    object Idle : SessionUiState
    object Loading : SessionUiState
    data class Success(val sessionData: ActiveSessionResponse) : SessionUiState
    data class Error(val message: String) : SessionUiState
}

// 2. Main ViewModel class
class SessionViewModel(private val repository: EmergencyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Idle)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private val _checkInState = MutableStateFlow<SessionUiState>(SessionUiState.Idle)
    val checkInState: StateFlow<SessionUiState> = _checkInState.asStateFlow()

    fun checkCurrentSession(token: String) {
        viewModelScope.launch {
            _uiState.value = SessionUiState.Loading

            repository.fetchActiveSession(token)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _uiState.value = SessionUiState.Success(response)
                    } else {
                        _uiState.value = SessionUiState.Error(response.message)
                    }
                }
                .onFailure { exception ->
                    _uiState.value = SessionUiState.Error(
                        exception.localizedMessage ?: "Failed to connect to server"
                    )
                }
        }
    }

    fun checkIn(token: String, groupSize: Int, vehicleRegistration: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _checkInState.value = SessionUiState.Loading

            repository.checkIn(token, groupSize, vehicleRegistration)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _checkInState.value = SessionUiState.Success(response)
                        onSuccess()
                    } else {
                        _checkInState.value = SessionUiState.Error(response.message)
                    }
                }
                .onFailure { exception ->
                    _checkInState.value = SessionUiState.Error(
                        exception.localizedMessage ?: "Failed to connect to server"
                    )
                }
        }
    }
}