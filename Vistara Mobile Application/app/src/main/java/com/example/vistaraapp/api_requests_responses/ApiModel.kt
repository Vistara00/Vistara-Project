package com.example.vistaraapp.api_requests_responses

import com.google.gson.annotations.SerializedName

// This file is for all api responses and requests
data class ProceedToPayment(
    val checkInDate: String,         // "2026-06-15"
    val checkOutDate: String,        // "2026-06-17"
    val groupSize: Int,              // 2
    val vehicleRegistration: String, // "KBC 123A"
    val paymentMethod: String,       // "MPESA"
    val amount: Double               // 2500.0
)

// Bookings data container
data class BookingsResponse(
    @field:SerializedName("success") val success: Boolean,
    @field:SerializedName("message") val message: String,
    @field:SerializedName("data") val data: List<BookingData>,
    @field:SerializedName("timestamp") val timestamp: String,
    @field:SerializedName("statusCode") val statusCode: Int
)

// Individual booking details inside the list
data class BookingData(
    @field:SerializedName("id") val id: Int?,
    @field:SerializedName("userId") val userId: Int?,
    @field:SerializedName("bookingReference") val bookingReference: String?,
    @field:SerializedName("amount") val amount: Double?,
    @field:SerializedName("groupSize") val groupSize: Int?,
    @field:SerializedName("checkInDate") val checkInDate: String?,
    @field:SerializedName("checkOutDate") val checkOutDate: String?,
    @field:SerializedName("vehicleRegistration") val vehicleRegistration: String?,
    @field:SerializedName("paymentMethod") val paymentMethod: String?,
    @field:SerializedName("paymentReference") val paymentReference: String?,
    @field:SerializedName("paymentStatus") val paymentStatus: String?,
    @field:SerializedName("bookingStatus") val bookingStatus: String?,
    @field:SerializedName("createdAt") val createdAt: String?
)