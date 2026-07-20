package com.example.vistaraapp.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.EmergencyTrackingService
import com.example.vistaraapp.api_requests_responses.BookingData
import com.example.vistaraapp.api_requests_responses.NotificationListResponse
import com.example.vistaraapp.api_requests_responses.toUiModel
import com.example.vistaraapp.repositories.BookingRepository
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.entities_dataclass.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log.e
import com.example.vistaraapp.api.BookingRequest


sealed class BookingUiState {
    object Loading : BookingUiState()
    data class Success(val bookings: List<BookingData>) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(private val repository: BookingRepository) : ViewModel() {

    // BOOKING STATE HOOKS
    private val _uiState = mutableStateOf<BookingUiState>(BookingUiState.Loading)
    val uiState: State<BookingUiState> = _uiState

    private val _cancellationStatus = mutableStateOf<String?>(null)
    val cancellationStatus: State<String?> = _cancellationStatus

    // EMERGENCY SOS STATE HOOKS
    private val _sosStatus = mutableStateOf<String?>(null)
    val sosStatus: State<String?> = _sosStatus

    // LIVE NOTIFICATION FEED STATE FLOWS
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoadingNotifications = MutableStateFlow(false)
    val isLoadingNotifications: StateFlow<Boolean> = _isLoadingNotifications.asStateFlow()

    // UNREAD ALERTS STATE FLOWS (FOR COUNTERS / BADGES)
    private val _unreadNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val unreadNotifications: StateFlow<List<NotificationItem>> = _unreadNotifications.asStateFlow()

    private val _isLoadingUnread = MutableStateFlow(false)
    val isLoadingUnread: StateFlow<Boolean> = _isLoadingUnread.asStateFlow()

    //  ADDED: LIGHTWEIGHT UNREAD COUNTER BADGE STATE
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // CUSTOM PAYMENT PHONE STATE HOOKS
    private val _customPaymentPhone = mutableStateOf("")
    val customPaymentPhone: State<String> = _customPaymentPhone


    // VALUE CHANGE LISTENERS

    fun onCustomPhoneChanged(newNumber: String) {
        _customPaymentPhone.value = newNumber
    }

    //qr code
    private val _qrCodeState = MutableStateFlow<String?>(null)
    val qrCodeState: StateFlow<String?> = _qrCodeState

    private val _qrError = MutableStateFlow<String?>(null)
    val qrError: StateFlow<String?> = _qrError

    // OPERATIONS

    // 1. OPERATION: FETCH USER BOOKINGS
    fun fetchBookings(jwtToken: String) {
        Log.d("BOOKING_DEBUG", "The raw token received by ViewModel is: '$jwtToken'")
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                val formattedToken = if (jwtToken.startsWith("Bearer ")) jwtToken else "Bearer $jwtToken"
                val response = repository.getUserBookings(formattedToken)

                if (response.success) {
                    _uiState.value = BookingUiState.Success(response.data)
                } else {
                    _uiState.value = BookingUiState.Error(response.message)
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    Log.d("BOOKING_DEBUG", "Received 404, interpreting as empty bookings list")
                    _uiState.value = BookingUiState.Success(emptyList())
                } else {
                    Log.e("BOOKING_DEBUG", "Network HTTP error", e)
                    _uiState.value = BookingUiState.Error("Failed to load bookings: ${e.localizedMessage}")
                }
            } catch (e: Exception) {
                Log.e("BOOKING_DEBUG", "Network execution failed", e)
                _uiState.value = BookingUiState.Error("Failed to load bookings: ${e.localizedMessage}")
            }
        }
    }

    // 2. OPERATION: CANCEL BOOKING
    fun cancelBooking(jwtToken: String, bookingId: String) {
        Log.d("BOOKING_DEBUG", "Canceling booking ID: $bookingId")
        viewModelScope.launch {
            try {
                val formattedToken = if (jwtToken.startsWith("Bearer ")) jwtToken else "Bearer $jwtToken"
                val response = repository.cancelBooking(formattedToken, bookingId)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()
                    if (body?.success == true) {
                        _cancellationStatus.value = "SUCCESS: ${body.message}"
                        fetchBookings(jwtToken) // Refresh list automatically
                    } else {
                        _cancellationStatus.value = "FAILED: ${body?.message}"
                    }
                } else {
                    _cancellationStatus.value = "SERVER_ERROR: Code ${response.code()}"
                }
            } catch (e: Exception) {
                _cancellationStatus.value = "ERROR: ${e.localizedMessage}"
            }
        }
    }

    // TRIGGER SOS ALERT WITH GPS COORDINATES
    fun triggerSosAlert(jwtToken: String, latitude: Double, longitude: Double, alertType: String, message: String? = null) {
        Log.d("SOS_DEBUG", "Triggering SOS at Lat: $latitude, Lon: $longitude with message: $message")
        viewModelScope.launch {
            _sosStatus.value = "SENDING"
            try {
                val formattedToken = if (jwtToken.startsWith("Bearer ")) jwtToken else "Bearer $jwtToken"
                val response = repository.sendSosAlert(formattedToken, latitude, longitude, alertType, message)

                if (response.isSuccessful && response.body() != null) {
                    val serverMessage = response.body()?.message ?: "Emergency services notified."
                    _sosStatus.value = "SUCCESS: $serverMessage"
                } else {
                    _sosStatus.value = "FAILED: Server error code ${response.code()}"
                }
            } catch (e: Exception) {
                _sosStatus.value = "ERROR: ${e.localizedMessage}"
            }
        }
    }

    // 3. OPERATION: LIVE NOTIFICATIONS HANDLER
    fun fetchAllNotifications(token: String) {
        viewModelScope.launch {
            _isLoadingNotifications.value = true
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.fetchAllNotifications(formattedToken)

                if (response != null && response.isSuccessful && response.body() != null) {
                    val networkEnvelope: NotificationListResponse = response.body()!!
                    val uiList = networkEnvelope.data.map { it.toUiModel() }
                    _notifications.value = uiList
                    Log.d("BookingViewModel", "Notifications synced and mapped successfully: ${uiList.size} items")
                } else {
                    Log.e("BookingViewModel", "Server error loading notifications")
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Network failure fetching notifications", e)
            } finally {
                _isLoadingNotifications.value = false
            }
        }
    }

    // 4. OPERATION: FETCH UNREAD NOTIFICATIONS ONLY
    fun fetchUnreadNotifications(token: String) {
        viewModelScope.launch {
            _isLoadingUnread.value = true
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.fetchUnreadNotifications(formattedToken)

                if (response != null && response.isSuccessful && response.body() != null) {
                    val networkEnvelope: NotificationListResponse = response.body()!!
                    val uiList = networkEnvelope.data.map { it.toUiModel() }
                    _unreadNotifications.value = uiList

                    // OPTIMIZATION: Keep count synchronized with the real list size
                    _unreadCount.value = uiList.size
                    Log.d("BookingViewModel", "Unread alerts synced successfully: ${uiList.size} items")
                } else {
                    Log.e("BookingViewModel", "Server error loading unread alerts")
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Network failure fetching unread alerts", e)
            } finally {
                _isLoadingUnread.value = false
            }
        }
    }
    // QR code — called after confirmed payment
    fun loadQrCode(token: String, bookingId: String){
        viewModelScope.launch {
            try {
                val response = repository.getQrCode(token, bookingId)
                if (response.isSuccessful) {

                    _qrCodeState.value = response.body()?.data?.qrCodeBase64
                }
            } catch (e: Exception) {
                _qrError.value = "Failed to load QR code. Please check your internet connection and try again."
                Log.e("QRCode", "Error fetching QR code", e)
            }
        }
    }

    // ADDED: OPERATION #6 TO FETCH ONLY THE LIGHTWEIGHT COUNTER PAYLOAD
    fun fetchUnreadNotificationsCount(token: String) {
        viewModelScope.launch {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.countUnreadNotifications(formattedToken)

                if (response != null && response.isSuccessful && response.body() != null) {
                    _unreadCount.value = response.body()?.data ?: 0
                    Log.d("BookingViewModel", "Lightweight unread count synchronized: ${_unreadCount.value}")
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Network failure fetching unread count", e)
            }
        }
    }

    // 5. OPERATION: INITIATE MPESA PAYMENT FOR AN EXISTING BOOKING
    fun initiateMpesaPayment(jwtToken: String, bookingReference: String) {
        viewModelScope.launch {
            try {
                val currentBookings = (uiState.value as? BookingUiState.Success)?.bookings ?: emptyList()
                val targetBooking = currentBookings.find { it.bookingReference == bookingReference }
                val amount = targetBooking?.amount ?: 10.0 // Fallback minimum amount

                val formattedToken = if (jwtToken.startsWith("Bearer ")) jwtToken else "Bearer $jwtToken"
                var rawPhone = ""

                // Check if a custom billing number has been typed into the TextField state hook
                if (_customPaymentPhone.value.isNotBlank()) {
                    rawPhone = _customPaymentPhone.value
                } else {
                    // Fallback directly to profile data records
                    val profileResponse = RetrofitClient.profileInstance.getProfileDetails(formattedToken)
                    if (profileResponse.isSuccessful) {
                        val rawBody = profileResponse.body() as? Map<*, *>
                        val dataMap = rawBody?.get("data") as? Map<*, *>
                        rawPhone = (dataMap?.get("phoneNumber") ?: "").toString()
                    }
                }

                if (rawPhone.isBlank()) {
                    _cancellationStatus.value = "PAYMENT FAILED: No valid phone number found"
                    return@launch
                }

                // Format phone string structure cleanly to 254...
                var formattedPhone = rawPhone.replace(Regex("[^0-9]"), "")
                if (formattedPhone.startsWith("0")) {
                    formattedPhone = "254" + formattedPhone.substring(1)
                } else if (formattedPhone.startsWith("+254")) {
                    formattedPhone = formattedPhone.substring(1)
                } else if (!formattedPhone.startsWith("254")) {
                    formattedPhone = "254$formattedPhone"
                }

                // Compile and send Mpesa STK payload data mapping
                val mpesaPayload = com.example.vistaraapp.api.MpesaPushRequest(
                    amount = amount.coerceAtLeast(10.0),
                    phoneNumber = formattedPhone,
                    bookingReference = bookingReference,
                    accountReference = bookingReference,
                    transactionDesc = "Booking Payment"
                )

                val mpesaResponse = RetrofitClient.mpesaInstance.initiateStkPush(formattedToken, mpesaPayload)

                if (mpesaResponse.isSuccessful && mpesaResponse.body()?.success == true) {
                    _cancellationStatus.value = "PAYMENT SUCCESS: STK push sent successfully!"
                    _customPaymentPhone.value = "" // Flush input cleanly on complete success
                    fetchBookings(jwtToken) // Refresh to update statuses
                } else {
                    val errorMsg = mpesaResponse.body()?.message ?: "M-Pesa STK push request failed."
                    _cancellationStatus.value = "PAYMENT FAILED: $errorMsg"
                }
            } catch (e: Exception) {
                _cancellationStatus.value = "PAYMENT ERROR: ${e.localizedMessage}"
            }
        }
    }
    //6 .mark notifications as read
    fun markNotificationAsRead(token: String, notificationId: String) {
        viewModelScope.launch {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.markNotificationAsRead(formattedToken, notificationId)

                if (response != null && response.isSuccessful) {
                    Log.d("Notification", "Marked ID $notificationId as read")
                    // Refresh count and lists so the UI updates immediately
                    fetchUnreadNotificationsCount(token)
                    fetchAllNotifications(token)
                    fetchUnreadNotifications(token)
                }
            } catch (e: Exception) {
                Log.e("Notification", "Failed to mark as read", e)
            }
        }
    }

    // Bulk mark as read
    fun markMultipleNotificationsAsRead(token: String, notificationIds: List<String>) {
        viewModelScope.launch {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                var successAny = false
                for (id in notificationIds) {
                    val response = repository.markNotificationAsRead(formattedToken, id)
                    if (response != null && response.isSuccessful) {
                        Log.d("Notification", "Marked ID $id as read in bulk")
                        successAny = true
                    }
                }
                if (successAny) {
                    fetchUnreadNotificationsCount(token)
                    fetchAllNotifications(token)
                    fetchUnreadNotifications(token)
                }
            } catch (e: Exception) {
                Log.e("Notification", "Failed to mark bulk notifications as read", e)
            }
        }
    }

    // Mark all notifications as read using the API endpoint
    fun markAllNotificationAsRead(token: String, id: String) {
        viewModelScope.launch {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.markAllNotificationAsRead(formattedToken, id)
                if (response != null && response.isSuccessful) {
                    Log.d("Notification", "Marked all notifications as read for ID $id")
                    fetchUnreadNotificationsCount(token)
                    fetchAllNotifications(token)
                    fetchUnreadNotifications(token)
                }
            } catch (e: Exception) {
                Log.e("Notification", "Failed to mark all notifications as read", e)
            }
        }
    }

    fun startEmergencyTracking(context: Context, token: String, sessionId: Long) {
        val intent = Intent(context, EmergencyTrackingService::class.java).apply {
            putExtra("AUTH_TOKEN", token)
            putExtra("SESSION_ID", sessionId)
        }

        // Fix for API level 26 requirement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopEmergencyTracking(context: Context) {
        val intent = Intent(context, EmergencyTrackingService::class.java).apply {
            action = "ACTION_STOP_SOS"
        }
        context.startService(intent)
    }

    // STATUS CLEANUP UTILITIES
    fun clearCancellationStatus() { _cancellationStatus.value = null }

    fun clearSosStatus() { _sosStatus.value = null }
}