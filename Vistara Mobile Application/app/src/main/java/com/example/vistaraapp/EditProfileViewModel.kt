package com.example.vistaraapp.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val dao: ContactDao
) : ViewModel() {

    private val _state = MutableStateFlow(ContactState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: ContactEvent) {
        when (event) {
            is ContactEvent.SetFullName -> {
                _state.update { it.copy(fullName = event.fullName) }
            }
            is ContactEvent.SetIdNumber -> {
                _state.update { it.copy(idNumber = event.idNumber) }
            }
            is ContactEvent.SetPhoneNumber -> {
                _state.update { it.copy(phoneNumber = event.phoneNumber) }
            }
            is ContactEvent.SetEmergencyNumber -> {
                _state.update { it.copy(emergencyNumber = event.emergencyNumber) }
            }
            is ContactEvent.SetEmail -> {
                _state.update { it.copy(email = event.email) }
            }
            ContactEvent.SaveContact -> {
                saveUserProfileToLocalDb()
            }
            else -> { /* Handles other events safely */ }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            dao.getContactsOrderedByFullName().collect { contactsList ->
                if (contactsList.isNotEmpty()) {
                    val profile = contactsList.first()
                    _state.update {
                        it.copy(
                            fullName = profile.fullName,
                            idNumber = profile.idNumber,
                            phoneNumber = profile.phoneNumber,
                            emergencyNumber = profile.emergencyNumber,
                            email = profile.email
                        )
                    }
                }
            }
        }
    }

    private fun saveUserProfileToLocalDb() {
        val currentState = _state.value

        viewModelScope.launch {
            // FIXED: Explicitly set id to 1 since ContactState doesn't track row IDs.
            // This forces Room to overwrite the single user profile row dynamically.
            val contactEntity = Contact(
                id = 1,
                fullName = currentState.fullName,
                email = currentState.email,
                phoneNumber = currentState.phoneNumber,
                idNumber = currentState.idNumber,
                emergencyNumber = currentState.emergencyNumber,
                isCurrentUser = true
            )
            dao.upsertContact(contactEntity)
        }
    }

    fun saveProfileToApi(
        fullName: String,
        phone: String,
        emergencyName: String,
        emergencyPhone: String,
        nationalId: String
    ) {
        viewModelScope.launch {
            try {
                // Your network client API endpoint calls can safely be placed here
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}