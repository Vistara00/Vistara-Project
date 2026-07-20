package com.example.vistaraapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.viewmodels.LoginEvent
import com.example.vistaraapp.viewmodels.LoginViewModel
import com.example.vistaraapp.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val textLight = MaterialTheme.colorScheme.onSurfaceVariant // Lighter grey for placeholder text
    val lightGray = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFF6F6F6) // Card background color

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) {
            val destination = if (TokenManager.isRangerRole(state.role)) {
                "ranger_dashboard"
            } else {
                "home"
            }

            onNavigateToDashboard(destination)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Header
            Text(
                text = "Vistara",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = brandGreen,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your Safety, Our Priority",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Card Container
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
                    
                    // Email Input Field
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(LoginEvent.SetEmail(it)) },
                        label = { Text(text = "Email") },
                        placeholder = { Text(text = "Enter your Email", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = brandGreen,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = brandGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input Field
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(LoginEvent.SetPassword(it)) },
                        label = { Text(text = "Password") },
                        placeholder = { Text(text = "Password", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = "Toggle visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = brandGreen,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = brandGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password Text
                    Text(
                        text = "Forgot Password?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = brandGreen,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onNavigateToResetPassword() }
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Display Message Banner
                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // LOGIN Button
                    Button(
                        onClick = { viewModel.onEvent(LoginEvent.LoginClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brandGreen,
                            disabledContainerColor = brandGreen.copy(alpha = 0.5f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "LOGIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer / Route to Registration View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Register",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandGreen,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}