package com.example.vistaraapp.api

import com.example.vistaraapp.api_requests_responses.ActiveSessionResponse
import com.example.vistaraapp.api_requests_responses.CheckInRequest
import com.example.vistaraapp.api_requests_responses.SosRequest
import com.example.vistaraapp.api_requests_responses.SosResponse
import com.example.vistaraapp.api_requests_responses.BookingsResponse
import com.example.vistaraapp.api_requests_responses.NotificationListResponse
import com.example.vistaraapp.ProfileNetworkRequest
import com.example.vistaraapp.RangerAlert
import com.example.vistaraapp.RangerAlertResponse
import com.example.vistaraapp.AlertStatisticsResponse
import com.example.vistaraapp.ClaimAlertResponse
import com.example.vistaraapp.QrCheckInRequest
import com.example.vistaraapp.QrCheckInResponse
import com.example.vistaraapp.ResolveAlertRequest
import com.example.vistaraapp.ResolveAlertResponse
import com.example.vistaraapp.api_requests_responses.UnreadCountResponse
import com.example.vistaraapp.api_requests_responses.NotificationReadResponse
import com.example.vistaraapp.api_requests_responses.TrackingUpdateRequest
import com.example.vistaraapp.api_requests_responses.TrackingUpdateResponse
import com.example.vistaraapp.api_requests_responses.BookingData
import com.example.vistaraapp.QrCodeResponse
import com.example.vistaraapp.api_requests_responses.ScanQrCodeResponse
import com.example.vistaraapp.api_requests_responses.ScanQrCodeRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

// 1. DATA MODELS FOR BOOKINGS & PAYMENTS

data class BookingRequest(
    val checkInDate: String,
    val checkOutDate: String,
    val groupSize: Int,
    val vehicleRegistration: String,
    val paymentMethod: String,
    val amount: Double
)

data class BookingCreationResponse(
    val success: Boolean?,
    val message: String?,
    val data: BookingData?
)

// Mpesa
data class MpesaPushRequest(
    val amount: Double,
    val phoneNumber: String,
    val bookingReference: String,
    val accountReference: String,
    val transactionDesc: String
)

data class MpesaPushResponse(
    val success: Boolean,
    val message: String,
    val checkoutRequestID: String?
)

// 2. BOOKING, EMERGENCY & NOTIFICATION ENDPOINTS SERVICE
interface ApiService {

    @Headers("ngrok-skip-browser-warning: true")
    @GET("bookings")
    suspend fun getBookings(
        @Header("Authorization") token: String
    ): BookingsResponse

    @Headers("ngrok-skip-browser-warning: true")
    @POST("bookings")
    suspend fun proceedToPayment(
        @Header("Authorization") bearerToken: String,
        @Body bookingData: BookingRequest
    ): Response<BookingCreationResponse>

    @Headers("ngrok-skip-browser-warning: true")
    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Header("Authorization") bearerToken: String,
        @Path("bookingId") bookingId: String
    ): Response<BookingCreationResponse>

    @Headers("ngrok-skip-browser-warning: true")
    @POST("emergency/sos")
    suspend fun triggerSos(
        @Header("Authorization") bearerToken: String,
        @Body sosData: SosRequest
    ): Response<SosResponse>

    // notifications
    @Headers("ngrok-skip-browser-warning: true")
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") bearerToken: String
    ): Response<NotificationListResponse>

    // unread notifications
    @Headers("ngrok-skip-browser-warning: true")
    @GET("notifications/unread")
    suspend fun getUnreadNotifications(
        @Header("Authorization") bearerToken: String
    ): Response<NotificationListResponse>

    @Headers("ngrok-skip-browser-warning: true")
    @GET("notifications/unread-count")
    suspend fun countNotifications(
        @Header("Authorization") bearerToken: String
    ): Response<UnreadCountResponse>

    //mark a  notification as read
    @Headers("ngrok-skip-browser-warning: true")
    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    // mark all notifications as read
    @Headers("ngrok-skip-browser-warning: true")
    @PUT("notifications/{id}/read-all")
    suspend fun markAllNotificationAsRead(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<NotificationReadResponse>

    //tracking
    @Headers("ngrok-skip-browser-warning: true")
    @POST("tracking/update")
    suspend fun updateTracking(
        @Header("Authorization") bearerToken: String,
        @Body request: TrackingUpdateRequest
    ): Response<TrackingUpdateResponse>

    //Ranger Assigned
    @GET("ranger/alerts/assigned")
    suspend fun getAssignedAlerts(
        @Header("Authorization") token: String
    ): Response<RangerAlertResponse>

    //Status of
    @GET("ranger/alerts/assigned/status/{status}")
    suspend fun getAlertsByStatus(
        @Header("Authorization") token: String,
        @Path("status") status: String
    ): Response<RangerAlertResponse>

    //RangerStats
    @GET("ranger/alerts/stats")
    suspend fun noOfSOS(
        @Header("Authorization") token: String,
    ): Response<AlertStatisticsResponse>

    //Ranger claim
    @POST("ranger/alerts/{alertId}/claim")
    suspend fun assignToMe(
        @Header("Authorization") token: String,
        @Path("alertId") alertId: Long
    ): ClaimAlertResponse

    //responding to resolved
    @PUT("ranger/alerts/{alertId}/resolve")
    suspend fun resolveAlert(
        @Header("Authorization") token: String,
        @Path("alertId") alertId: Long,
        @Body request: ResolveAlertRequest
    ): ResolveAlertResponse

    // All pending (unassigned) emergency alerts
    @GET("emergency/pending")
    suspend fun getPendingEmergencies(
        @Header("Authorization") token: String
    ): Response<RangerAlertResponse>

    //generate qr code
    @GET("bookings/{bookingId}/qr")
            suspend fun getQrCode(
        @Path("bookingId") bookingId: String,
        @Header("Authorization") token: String

            ):Response<QrCodeResponse>

    //Ranger scan qr code
    @POST("bookings/scan-qr")
    suspend fun scanQrCode(
        @Header("Authorization") token: String,
        @Body request: ScanQrCodeRequest
    ): Response<ScanQrCodeResponse>
    // qr scan checkin
    @POST("bookings/qr-checkin")
    suspend fun checkInWithQr(
        @Header("Authorization") token: String,
        @Body request: QrCheckInRequest
    ): Response<QrCheckInResponse>

    //general ranger alerts
    @Headers("ngrok-skip-browser-warning: true")
    @GET("ranger/alerts/all")
    suspend fun getAllAlerts(
        @Header("Authorization") token: String
    ): Response<com.example.vistaraapp.api_requests_responses.AlertsGeneralDto>
}
// 3. PROFILE ENDPOINTS SERVICE
interface ProfileApiService {

    @PUT("profile")
    suspend fun saveProfileDetails(
        @Header("Authorization") bearerToken: String,
        @Body profileData: ProfileNetworkRequest,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<ResponseBody>

    @GET("profile")
    suspend fun getProfileDetails(
        @Header("Authorization") bearerToken: String,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<Map<String, Any>>
}

// 4. M-PESA STK PUSH SERVICE
interface VistaraApi {
    @Headers("ngrok-skip-browser-warning: true")
    @POST("payments/mpesa/stkpush")
    suspend fun initiateStkPush(
        @Header("Authorization") bearerToken: String,
        @Body request: MpesaPushRequest
    ): Response<MpesaPushResponse>
}

// 5. SESSION ID SERVICE
interface SessionApi {
    @Headers("ngrok-skip-browser-warning: true")
    @GET("visitor/active-session")
    suspend fun checkActiveSession(
        @Header("Authorization") bearerToken: String
    ): Response<ActiveSessionResponse>

    @Headers("ngrok-skip-browser-warning: true")
    @POST("visitor/check-in")
    suspend fun checkIn(
        @Header("Authorization") bearerToken: String,
        @Body request: CheckInRequest
    ): Response<ActiveSessionResponse>
}
