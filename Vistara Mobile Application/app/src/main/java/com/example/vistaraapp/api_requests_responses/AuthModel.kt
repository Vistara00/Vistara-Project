package com.example.vistaraapp.api_requests_responses
// DATA MODELS

// Request model for registration
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val nationalId: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String
)

// Login request
data class LoginRequest(
    val email: String,
    val password: String
)

// FORGOT PASSWORD REQUEST MODEL
data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String? = null
)
// Response model for registration
data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val userId: Int? = null,
    val token: String? = null
)

// Response for login
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val userId: Int? = null,
    val token: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val data: LoginData? = null,
    val role: String? = null
) {
    // Helper to get token whether it's at the root or nested in data
    fun getActualToken(): String? {
        return token ?: data?.token
    }

    // Helper to get role whether it's at the root or nested in data
    fun getActualRole(): String? {
        return role ?: data?.role
    }
}

data class LoginData(
    val token: String? = null,
    val userId: Int? = null,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null
)