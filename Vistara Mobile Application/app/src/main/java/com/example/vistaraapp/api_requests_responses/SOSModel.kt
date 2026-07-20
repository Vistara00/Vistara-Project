package com.example.vistaraapp.api_requests_responses

// The structured data body sent over ngrok
data class SosRequest(
        val latitude: Double,
        val longitude: Double,
        val alertType: String,
        val sessionId:  Int,
        val message: String = "Emergency SOS Triggered from Mobile App"
)

// The structure received back from the backend
data class SosResponse(
        val success: Boolean,
        val message: String,
        val timestamp: String
)