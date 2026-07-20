package com.example.vistaraapp.api

import com.example.vistaraapp.api_requests_responses.ForgotPasswordRequest
import com.example.vistaraapp.api_requests_responses.ForgotPasswordResponse
import com.example.vistaraapp.api_requests_responses.LoginRequest
import com.example.vistaraapp.api_requests_responses.LoginResponse
import com.example.vistaraapp.api_requests_responses.RegisterRequest
import com.example.vistaraapp.api_requests_responses.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiAuthService {
    @POST("auth/register/tourist")
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse



}