package com.example.vistaraapp.api_requests_responses


// 1. Top-level wrapper response
data class ActiveSessionResponse(
    val success: Boolean,
    val message: String,
    val data: SessionData?,
    val timestamp: String,
    val statusCode: Int
)

// 2. The core session tracking data body
data class SessionData(
    val sessionId: Int?,
    val id: Int?,
    val user: UserProfileData?,
    val visitor: VisitorProfileData?,
    val checkInTime: String,
    val checkOutTime: String?,
    val active: Boolean,
    val groupSize: Int,
    val vehicleRegistration: String?,
    val lastKnownLocation: String?,
    val lastLocationUpdate: String?,
    val sosTriggered: Boolean,
    val hasEmergency: Boolean,
    val notes: String?,
    val booking: BookingDetails?,
    val createdAt: String?,
    val updatedAt: String?
)

// 3. User information segment
data class UserProfileData(
    val id: Int,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val nationalId: String,
    val role: String,
    val active: Boolean,
    val verified: Boolean,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val username: String
)

data class VisitorProfileData(
    val id: Int,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?
)

// 4. Embedded booking metrics
data class BookingDetails(
    val id: Int,
    val bookingReference: String,
    val bookingStatus: String,
    val paymentStatus: String,
    val paymentMethod: String,
    val amount: Double,
    val checkInDate: String,
    val checkOutDate: String,
    val adminNotes: String?
)

data class CheckInRequest(
    val groupSize: Int,
    val vehicleRegistration: String?
)