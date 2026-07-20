package com.example.vistaraapp.api_requests_responses

import com.google.gson.annotations.SerializedName

// The request body matching your JSON exactly
data class MpesaPushRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("accountReference") val accountReference: String,
    @SerializedName("transactionDesc") val transactionDesc: String
)

// A standard response body to handle what ngrok/Daraja returns
data class MpesaPushResponse(
    @SerializedName("MerchantRequestID") val merchantRequestId: String?,
    @SerializedName("CustomerRequestID") val customerRequestId: String?,
    @SerializedName("ResponseDescription") val responseDescription: String?,
    @SerializedName("ResponseCode") val responseCode: String?
)