package com.example.vistaraapp.api_requests_responses

import com.google.gson.annotations.SerializedName

// Request body sent to POST bookings/scan-qr
data class ScanQrCodeRequest(
    @SerializedName("qrData") val qrData: String  // the decoded string from the camera scan
)

// Response from POST bookings/scan-qr
data class ScanQrCodeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BookingDetails?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("statusCode") val statusCode: Int
)
