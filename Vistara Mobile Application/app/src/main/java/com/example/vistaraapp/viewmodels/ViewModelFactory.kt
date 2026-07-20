package com.example.vistaraapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.database.ContactViewModel
import com.example.vistaraapp.database.ContactDatabase
import com.example.vistaraapp.repositories.EmergencyRepository

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. Contact Database Flow
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            val database = ContactDatabase.getDatabase(application)
            return ContactViewModel(database.dao) as T
        }

        // 2. Active Session & SOS Flow
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            // Pulling local instances from your client configuration singleton
            val apiService = RetrofitClient.bookingInstance
            val sessionApi = RetrofitClient.sessionInstance

            // FIXED: Passing the variable 'apiService' instead of the class type token
            val emergencyRepository = EmergencyRepository(apiService, sessionApi)
            return SessionViewModel(emergencyRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}