package com.example.vistaraapp

import com.example.vistaraapp.api.RetrofitClient
import retrofit2.Response
import com.example.vistaraapp.QrCheckInResponse

suspend fun checkInWithQr(token: String, qrData: String): Response<QrCheckInResponse> {
    val request = QrCheckInRequest(qrData = qrData)
    return RetrofitClient.bookingInstance.checkInWithQr("Bearer $token", request)
}