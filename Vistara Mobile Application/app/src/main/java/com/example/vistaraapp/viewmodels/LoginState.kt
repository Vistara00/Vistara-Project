package com.example.vistaraapp.viewmodels

//the job of this file is to take user inputs (like email and password), trigger your new AuthRepository, and manage the UI state (loading, success, or error messages).

// Tracks what the Login UI looks like at any given second
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val role: String? = null,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val token: String? = null
)


// Simple commands sent from your Compose Screen UI to the ViewModel
//this states the functions the user can perform on the login screen
sealed interface LoginEvent {
    data class SetEmail(val email: String) : LoginEvent
    data class SetPassword(val password: String) : LoginEvent
    data object LoginClicked : LoginEvent
    data object ClearError : LoginEvent
}