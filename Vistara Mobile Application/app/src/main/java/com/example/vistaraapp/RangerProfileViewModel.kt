package com.example.vistaraapp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.data.SessionManager
import com.example.vistaraapp.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RangerProfileViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(RangerProfileState())
    val state: StateFlow<RangerProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val profile = authRepository.getContactById(1)
            if (profile != null) {
                _state.update {
                    it.copy(
                        fullName = profile.fullName,
                        email = profile.email,
                        phoneNumber = profile.phoneNumber,
                        role = "PARK_RANGER",
                        isLoading = false
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Profile not found"
                    )
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            val profile = authRepository.getContactById(1)
            if (profile != null) {
                authRepository.upsertContact(profile.copy(isCurrentUser = false))
            }
            onSuccess()
        }
    }
}

