package com.example.vistaraapp.repositories

import com.example.vistaraapp.QrCodeResponse
import com.example.vistaraapp.api.ApiService
import com.example.vistaraapp.api.BookingCreationResponse
import com.example.vistaraapp.api_requests_responses.BookingsResponse
import com.example.vistaraapp.api_requests_responses.NotificationListResponse
import com.example.vistaraapp.api_requests_responses.SosResponse
import com.example.vistaraapp.api_requests_responses.UnreadCountResponse
import com.example.vistaraapp.api_requests_responses.NotificationReadResponse
import retrofit2.Response
import okhttp3.ResponseBody

class BookingRepository(private val apiService: ApiService) {

    // 1. Fetch bookings
    suspend fun getUserBookings(token: String): BookingsResponse {
        return apiService.getBookings(token)
    }

    // 2. Cancel a booking
    suspend fun cancelBooking(token: String, bookingId: String): Response<BookingCreationResponse> {
        return apiService.cancelBooking(token, bookingId)
    }

    // 3. Send emergency SOS alert
    suspend fun sendSosAlert(token: String, lat: Double, lon: Double, alertType: String, message: String? = null): Response<SosResponse> {
        val sessionResponse = com.example.vistaraapp.api.RetrofitClient.sessionInstance.checkActiveSession(token)
        val sessionId = if (sessionResponse.isSuccessful) {
            val sessionData = sessionResponse.body()?.data
            sessionData?.sessionId ?: sessionData?.id ?: 0
        } else {
            0
        }

        val requestPayload = com.example.vistaraapp.api_requests_responses.SosRequest(
            latitude = lat,
            longitude = lon,
            alertType = alertType,
            sessionId = sessionId,
            message = message ?: "Emergency SOS Triggered from Mobile App"
        )
        return apiService.triggerSos(token, requestPayload)
    }

    // 4. Fetch all notifications feed
    suspend fun fetchAllNotifications(token: String): Response<NotificationListResponse>? {
        return try {
            apiService.getNotifications(token)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 5. Fetch unread notifications only
    suspend fun fetchUnreadNotifications(token: String): Response<NotificationListResponse>? {
        return try {
            apiService.getUnreadNotifications(token)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
//6.count unread notification
suspend fun countUnreadNotifications(token: String): Response<UnreadCountResponse>? {
    return try {
        apiService.countNotifications(token)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
    //7.mark as read
    suspend fun markNotificationAsRead(token: String, id: String): Response<ResponseBody>? {
        return try {
            apiService.markNotificationAsRead(token, id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //8.mark all notifications as read
    suspend fun markAllNotificationAsRead(token: String, id: String): Response<NotificationReadResponse>? {
        return try {
            apiService.markAllNotificationAsRead(token, id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // qr code generation
    suspend fun getQrCode(token: String,bookingId: String):Response<QrCodeResponse>{
        return apiService.getQrCode(bookingId,"Bearer $token")
    }

    // qr check-in
    suspend fun checkInWithQr(token: String, qrData: String): Response<com.example.vistaraapp.QrCheckInResponse> {
        val request = com.example.vistaraapp.QrCheckInRequest(qrData = qrData)
        return apiService.checkInWithQr("Bearer $token", request)
    }
}