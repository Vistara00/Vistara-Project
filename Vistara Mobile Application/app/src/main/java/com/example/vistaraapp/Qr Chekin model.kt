package com.example.vistaraapp

import com.google.gson.annotations.SerializedName

data class QrCheckInRequest(
    @SerializedName("qrData") val qrData: String
)