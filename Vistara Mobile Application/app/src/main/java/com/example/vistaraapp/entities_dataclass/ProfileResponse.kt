package com.example.vistaraapp.entities_dataclass


import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ProfileData,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("statusCode") val statusCode: Int
)

data class ProfileData(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("emergencyContactName") val emergencyContactName: String?,
    @SerializedName("emergencyContactPhone") val emergencyContactPhone: String?,
    @SerializedName("nationalId") val nationalId: String?,
    @SerializedName("role") val role: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("verified") val verified: Boolean
)