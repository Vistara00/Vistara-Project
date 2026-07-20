package com.example.vistaraapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.repositories.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QrCheckInUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CheckinWithQrViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrCheckInUiState())
    val uiState: StateFlow<QrCheckInUiState> = _uiState

    fun performQrCheckIn(token: String, qrToken: String) {
        viewModelScope.launch {
            _uiState.value = QrCheckInUiState(isLoading = true)
            try {
                val response = repository.checkInWithQr(token, qrToken)
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("QR_CHECKIN", "Check-in successful: $body")
                    _uiState.value = QrCheckInUiState(
                        successMessage = body?.message ?: "Check-in successful"
                    )
                } else {
                    Log.e("QR_CHECKIN", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    _uiState.value = QrCheckInUiState(
                        errorMessage = "Check-in failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Log.e("QR_CHECKIN", "Exception: ${e.message}")
                _uiState.value = QrCheckInUiState(errorMessage = e.message ?: "Unknown error")
            }
        }
    }
}