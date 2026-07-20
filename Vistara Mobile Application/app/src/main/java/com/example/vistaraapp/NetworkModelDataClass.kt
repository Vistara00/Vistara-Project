package com.example.vistaraapp
import com.google.gson.annotations.SerializedName

data class ProfileNetworkRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("emergencyContactName") val emergencyContactName: String,
    @SerializedName("emergencyContactPhone") val emergencyContactPhone: String,
    @SerializedName("nationalId") val nationalId: String
)
