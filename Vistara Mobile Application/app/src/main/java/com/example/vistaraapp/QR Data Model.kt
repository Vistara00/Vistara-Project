package com.example.vistaraapp

import com.google.gson.annotations.SerializedName

data class QrCodeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: QrCodeData?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("statusCode") val statusCode: Int
)

data class QrCodeData(
    @SerializedName("bookingId") val bookingId: Long,
    @SerializedName("bookingReference") val bookingReference: String,
    @SerializedName("qrCodeBase64") val qrCodeBase64: String,
    @SerializedName("visitorName") val visitorName: String?,
    @SerializedName("checkInDate") val checkInDate: String?,
    @SerializedName("checkOutDate") val checkOutDate: String?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("bookingStatus") val bookingStatus: String?
)