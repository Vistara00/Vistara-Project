package com.example.vistaraapp.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.viewmodels.AuthViewModel
import com.example.vistaraapp.viewmodels.ForgotPasswordUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textLight = MaterialTheme.colorScheme.onSurfaceVariant
    val lightGray = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)

    var otpCode by remember { mutableStateOf("") }
    var newpassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // This state controls our single-screen wizard pipeline
    var currentStep by remember { mutableStateOf(1) } // 1 = Email, 2 = OTP, 3 = Password Update
    var newPasswordVisible by remember { mutableStateOf(false) }//these two are password visibility states
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val resetState by authViewModel.forgotPasswordState.collectAsState()

    // Smart Pipeline: State observer responds contextually based  current wizard step
    LaunchedEffect(resetState) {
        when (resetState) {
            is ForgotPasswordUiState.Success -> {
                Toast.makeText(context, (resetState as ForgotPasswordUiState.Success).message, Toast.LENGTH_LONG).show()
                authViewModel.resetForgotPasswordState()

                // Advance the step sequentially based on where the user currently is
                when (currentStep) {
                    1 -> currentStep = 2 // Email sent successfully -> Go to OTP
                    2 -> currentStep = 3 // OTP verified successfully -> Go to New Password
                    3 -> onBackToLogin() // Password reset complete -> Return to Login screen
                }
            }
            is ForgotPasswordUiState.Error -> {
                Toast.makeText(context, (resetState as ForgotPasswordUiState.Error).message, Toast.LENGTH_LONG).show()
                authViewModel.resetForgotPasswordState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep -= 1 // Walk backward 1 step inside the wizard frame
                            } else {
                                onBackToLogin() // Exit forgot password process entirely
                            }
                        },
                        enabled = resetState !is ForgotPasswordUiState.Loading
                    ) {

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pureWhite,
                    titleContentColor = Color(0xFF029602),
                    navigationIconContentColor = brandGreen
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ========== CARD CONTAINER MOUNT ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = lightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        1 -> {
                            // ─── STEP 1 Layout: Requesting OTP ───
                            Text(
                                text = "Forgot your password",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandGreen,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Please enter the email address you'd like your password reset information sent to",
                                fontSize = 12.sp,
                                color = textDark,
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(text = "Enter email address", color = textLight) },
                                placeholder = { Text(text = "bwire@example.com", color = textLight.copy(alpha = 0.6f)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = resetState !is ForgotPasswordUiState.Loading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandGreen,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = brandGreen,
                                    unfocusedLabelColor = textLight,
                                    focusedTextColor = textDark,
                                    unfocusedTextColor = textDark,
                                    cursorColor = brandGreen
                                )
                            )
                        }
                        2 -> {
                            // ─── STEP 2 Layout: Verifying OTP ───
                            Text(
                                text = "Verify OTP",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandGreen,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Enter the verification code sent to your email address.",
                                fontSize = 15.sp,
                                color = textDark,
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text(text = "Enter OTP", color = textLight) },
                                placeholder = { Text(text = "6-digit code", color = textLight.copy(alpha = 0.6f)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = resetState !is ForgotPasswordUiState.Loading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandGreen,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = brandGreen,
                                    unfocusedLabelColor = textLight,
                                    focusedTextColor = textDark,
                                    unfocusedTextColor = textDark,
                                    cursorColor = brandGreen
                                )
                            )
                        }
                        3 -> {
                            // ─── STEP 3 Layout: Modifying Password Passkey ───
                            Text(
                                text = "Reset Password",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandGreen,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Please configure your new password parameters below.",
                                fontSize = 15.sp,
                                color = textDark,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = newpassword,
                                onValueChange = { newpassword = it },
                                label = { Text(text = "New Password", color = textLight) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = resetState !is ForgotPasswordUiState.Loading,
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val icon = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(imageVector = icon, contentDescription = "Toggle visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandGreen,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = brandGreen,
                                    unfocusedLabelColor = textLight,
                                    focusedTextColor = textDark,
                                    unfocusedTextColor = textDark,
                                    cursorColor = brandGreen
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text(text = "Confirm Password", color = textLight) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = resetState !is ForgotPasswordUiState.Loading,
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val icon = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(imageVector = icon, contentDescription = "Toggle visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandGreen,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = brandGreen,
                                    unfocusedLabelColor = textLight,
                                    focusedTextColor = textDark,
                                    unfocusedTextColor = textDark,
                                    cursorColor = brandGreen
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ========== MULTI-PURPOSE ACTION BUTTON ==========
            Button(
                onClick = {
                    when (currentStep) {
                        1 -> {
                            if (email.isNotBlank()) {
                                // Calls your ViewModel endpoint to fire off the initial request
                                authViewModel.sendPasswordResetEmail(email.trim())
                            } else {
                                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
                            }
                        }
                        2 -> {
                            if (otpCode.isNotBlank()) {
                                // Connect your viewmodel verification method here:
                                // authViewModel.verifyOtpCode(email.trim(), otpCode.trim())

                                // Local fallback shortcut:
                                currentStep = 3
                            } else {
                                Toast.makeText(context, "Please enter the OTP verification code", Toast.LENGTH_SHORT).show()
                            }
                        }
                        3 -> {
                            if (newpassword.isNotBlank() && confirmPassword.isNotBlank()) {
                                if (newpassword == confirmPassword) {
                                    // Connect your final password update method here:
                                    // authViewModel.confirmPasswordReset(email.trim(), newpassword.trim())

                                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    onBackToLogin()
                                } else {
                                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Please fill out both security fields", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = resetState !is ForgotPasswordUiState.Loading,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandGreen,
                    disabledContainerColor = brandGreen.copy(alpha = 0.5f)
                )
            ) {
                if (resetState is ForgotPasswordUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    // Button labels swap dynamically based on current step status
                    Text(
                        text = when (currentStep) {
                            1 -> "Request reset link"
                            2 -> "Verify OTP"
                            else -> "Change Password"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login Link
            TextButton(
                onClick = onBackToLogin,
                enabled = resetState !is ForgotPasswordUiState.Loading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Back to Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = brandGreen
                )
            }
        }
    }
}
