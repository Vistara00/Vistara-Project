package com.example.vistaraapp.api_requests_responses

// Requests
data class TrackingUpdateRequest(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val batteryLevel: Int,
    val sessionId: Long
)

//  response class
data class TrackingUpdateResponse(
    val success: Boolean,
    val message: String
)