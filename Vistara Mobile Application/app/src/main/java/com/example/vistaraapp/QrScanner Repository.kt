package com.example.vistaraapp.repositories

import com.example.vistaraapp.QrCheckInRequest
import com.example.vistaraapp.QrCheckInResponse
import com.example.vistaraapp.api.ApiService
import com.example.vistaraapp.api_requests_responses.ScanQrCodeRequest
import retrofit2.Response

class QrScannerRepository(
    private val apiService: ApiService
) {
    // qrData is the raw string decoded from the camera scan
    suspend fun scanQrCode(token: String, qrData: String) =
        apiService.scanQrCode("Bearer $token", ScanQrCodeRequest(qrData = qrData))

    suspend fun checkInWithQr(token: String, qrData: String): Response<QrCheckInResponse> =
        apiService.checkInWithQr("Bearer $token", QrCheckInRequest(qrData = qrData))
}