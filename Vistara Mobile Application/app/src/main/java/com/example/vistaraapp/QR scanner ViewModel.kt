package com.example.vistaraapp

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.api_requests_responses.BookingDetails
import com.example.vistaraapp.repositories.QrScannerRepository
import kotlinx.coroutines.launch

class QrScannerViewModel(
    private val repository: QrScannerRepository
) : ViewModel() {

    sealed class QrScannerUiState {
        object Idle : QrScannerUiState()
        object Loading : QrScannerUiState()
        data class Verified(
            val details: BookingDetails,
            val qrData: String
        ) : QrScannerUiState()
        data class CheckedIn(
            val message: String,
            val visitorName: String? = null,
            val bookingReference: String? = null,
            val status: String? = null
        ) : QrScannerUiState()
        data class Error(val message: String) : QrScannerUiState()
    }

    private val _uiState = mutableStateOf<QrScannerUiState>(QrScannerUiState.Idle)
    val uiState: State<QrScannerUiState> = _uiState

    // API 1: Verify QR Code via POST bookings/scan-qr
    fun verifyQrCode(token: String, qrData: String) {
        Log.d("QRScanner", "API 1: verifyQrCode initiated -> qrData: $qrData")
        viewModelScope.launch {
            _uiState.value = QrScannerUiState.Loading
            try {
                val response = repository.scanQrCode(token, qrData)
                Log.d("QRScanner", "API 1 Response Code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    val bookingDetails = response.body()?.data
                    if (bookingDetails != null) {
                        Log.d("QRScanner", "API 1 Verified details: $bookingDetails")
                        _uiState.value = QrScannerUiState.Verified(bookingDetails, qrData)
                    } else {
                        _uiState.value = QrScannerUiState.Error(response.body()?.message ?: "No booking data returned")
                    }
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e("QRScanner", "API 1 Error (${response.code()}): $errBody")
                    _uiState.value = QrScannerUiState.Error("QR verification failed (${response.code()}): $errBody")
                }
            } catch (e: Exception) {
                Log.e("QRScanner", "API 1 Exception", e)
                _uiState.value = QrScannerUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Alias for backward compatibility
    fun scanQrCode(token: String, qrData: String) = verifyQrCode(token, qrData)

    // API 2: Check-In Visitor via POST bookings/qr-checkin
    fun checkInVisitor(token: String, qrData: String) {
        Log.d("QRScanner", "API 2: checkInVisitor initiated -> qrData: $qrData")
        viewModelScope.launch {
            _uiState.value = QrScannerUiState.Loading
            try {
                val response = repository.checkInWithQr(token, qrData)
                Log.d("QRScanner", "API 2 Response Code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data
                    Log.d("QRScanner", "API 2 Check-in Success body: $body")
                    _uiState.value = QrScannerUiState.CheckedIn(
                        message = body?.message ?: "Check-in successful!",
                        visitorName = data?.visitorName,
                        bookingReference = data?.bookingReference,
                        status = data?.status
                    )
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e("QRScanner", "API 2 Error (${response.code()}): $errBody")
                    _uiState.value = QrScannerUiState.Error("Check-in failed (${response.code()}): $errBody")
                }
            } catch (e: Exception) {
                Log.e("QRScanner", "API 2 Exception", e)
                _uiState.value = QrScannerUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun reset() {
        _uiState.value = QrScannerUiState.Idle
    }
}