package com.example.vistaraapp.api_requests_responses

// The request payload matching your JSON structure exactly
data class BookingRequest(
    val checkInDate: String,
    val checkOutDate: String,
    val groupSize: Int,
    val vehicleRegistration: String,
    val paymentMethod: String,
    val amount: Double
)

// A standard generic response structure for modern backend APIs
data class BookingResponse(
    val status: String?,
    val message: String?,
    val data: Map<String, Any>?
)