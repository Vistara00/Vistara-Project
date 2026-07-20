package com.example.vistaraapp.repositories

import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.api_requests_responses.ForgotPasswordRequest
import com.example.vistaraapp.api_requests_responses.LoginRequest
import com.example.vistaraapp.api_requests_responses.LoginResponse
import com.example.vistaraapp.api_requests_responses.RegisterRequest
import com.example.vistaraapp.api_requests_responses.RegisterResponse
import com.example.vistaraapp.database.ContactDao
import com.example.vistaraapp.database.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val contactDao: ContactDao) {

    // Helper function to prevent raw HTML server crashes from leaking to the UI
    private fun parseHttpError(e: HttpException): String {
        val rawError = try {
            e.response()?.errorBody()?.string() ?: e.message()
        } catch (_: Exception) {
            e.message()
        }

        return if (rawError.contains("<!DOCTYPE html>") || rawError.contains("<html>")) {
            "Server gateway error (e.g., 502 Bad Gateway). Please ensure your local backend is running."
        } else {
            rawError
        }
    }

    // 1. USER REGISTRATION (ONLINE WITH LOCAL CACHING)
    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        nationalId: String,
        emergencyContactName: String,
        emergencyContactPhone: String
    ): RegisterResult {
        return withContext(Dispatchers.IO) {
            try {
                val sanitizedPhone = phoneNumber.replace(Regex("[^0-9]"), "")
                val sanitizedEmergencyPhone = emergencyContactPhone.replace(Regex("[^0-9]"), "")

                val request = RegisterRequest(
                    email = email,
                    password = password,
                    fullName = fullName,
                    phoneNumber = sanitizedPhone,
                    nationalId = nationalId,
                    emergencyContactName = emergencyContactName,
                    emergencyContactPhone = sanitizedEmergencyPhone
                )

                val response = RetrofitClient.instance.registerUser(request)

                if (response.success) {
                    val newUser = Contact(
                        id = 1,
                        fullName = fullName,
                        email = email.trim().lowercase(),
                        phoneNumber = sanitizedPhone,
                        idNumber = nationalId,
                        emergencyNumber = sanitizedEmergencyPhone,
                        isCurrentUser = false
                    )
                    contactDao.upsertContact(newUser)
                    RegisterResult.Success(response)
                } else {
                    RegisterResult.Error(response.message ?: "Registration failed")
                }
            } catch (e: HttpException) {
                RegisterResult.Error("Server error (${e.code()}): ${parseHttpError(e)}")
            } catch (_: IOException) {
                RegisterResult.Error("Network error: Please check your internet connection")
            } catch (e: Exception) {
                RegisterResult.Error(e.message ?: "An unknown registration error occurred")
            }
        }
    }

    // 2. USER LOGIN (WITH ANTI-OVERWRITE MERGE & OFFLINE FALLBACK)
    suspend fun loginUser(email: String, password: String): LoginResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = RetrofitClient.instance.loginUser(request)

                if (response.success) {
                    val userDetails = response.data
                    val name = userDetails?.fullName ?: response.fullName ?: ""
                    val mail = userDetails?.email ?: response.email ?: email

                    // Fetch existing profile data to keep registration numbers intact
                    val existingProfile = contactDao.getContactById(1)
                    val contact = Contact(
                        id = 1,
                        fullName = name,
                        email = mail.trim().lowercase(),
                        phoneNumber = existingProfile?.phoneNumber ?: "",
                        idNumber = existingProfile?.idNumber ?: "",
                        emergencyNumber = existingProfile?.emergencyNumber ?: "",
                        isCurrentUser = true
                    )
                    contactDao.upsertContact(contact)
                    LoginResult.Success(response)
                } else {
                    LoginResult.Error(response.message ?: "Invalid email or password")
                }
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    LoginResult.Error("Invalid email or password")
                } else {
                    LoginResult.Error("Server error (${e.code()}): ${parseHttpError(e)}")
                }
            } catch (_: IOException) {
                // Intercept network failure and safely handle local authentication fallback
                val existingProfile = contactDao.getContactById(1)
                if (existingProfile != null && existingProfile.email == email.trim().lowercase()) {
                    LoginResult.Success(
                        LoginResponse(
                            success = true,
                            message = "Logged in offline. Some features may be limited.",
                            fullName = existingProfile.fullName,
                            email = existingProfile.email
                        )
                    )
                } else {
                    LoginResult.Error("Network error: Please check your internet connection")
                }
            } catch (e: Exception) {
                LoginResult.Error(e.message ?: "An unexpected login error occurred")
            }
        }
    }

    // 3. FORGOT PASSWORD OPERATION
    suspend fun forgotPassword(email: String): ForgotPasswordResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = ForgotPasswordRequest(email = email)
                val response = RetrofitClient.instance.forgotPassword(request)

                if (response.success) {
                    ForgotPasswordResult.Success(response.message ?: "A reset link has been sent to your email.")
                } else {
                    ForgotPasswordResult.Error(response.message ?: "Failed to process request.")
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    ForgotPasswordResult.Error("No account found with this email address.")
                } else {
                    ForgotPasswordResult.Error("Server error (${e.code()}): ${parseHttpError(e)}")
                }
            } catch (_: IOException) {
                ForgotPasswordResult.Error("Network error: Please verify your internet connectivity.")
            } catch (e: Exception) {
                ForgotPasswordResult.Error(e.message ?: "An unexpected recovery error occurred")
            }
        }
    }

    // 4. VIEWMODEL ACCESS GATEWAY
    // Exposes the query helper function so the AuthViewModel can check user persistence on app launch
    suspend fun getContactById(id: Int): Contact? = contactDao.getContactById(id)

    suspend fun upsertContact(contact: Contact) {
        contactDao.upsertContact(contact)
    }
}

// RESULT SEALED CLASSES REQUIRED BY THE AUTH_VIEW_MODEL

sealed class RegisterResult {
    data class Success(val response: RegisterResponse) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

sealed class LoginResult {
    data class Success(val response: LoginResponse) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class ForgotPasswordResult {
    data class Success(val message: String) : ForgotPasswordResult()
    data class Error(val message: String) : ForgotPasswordResult()
}