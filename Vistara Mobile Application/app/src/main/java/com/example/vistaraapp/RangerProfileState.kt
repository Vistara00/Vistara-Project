package com.example.vistaraapp

data class RangerProfileState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "PARK_RANGER",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)