package com.example.vistaraapp.screens

import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vistaraapp.database.ContactEvent
import com.example.vistaraapp.database.ContactViewModel
import com.example.vistaraapp.viewmodels.AuthViewModel
import com.example.vistaraapp.viewmodels.RegisterUiState
import com.example.vistaraapp.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    contactViewModel: ContactViewModel
) {
    val context = LocalContext.current

    // State variables for each input field
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nationalIdNo by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe registration state
    val registerState by authViewModel.registerState.collectAsState()

    // Handle registration state changes
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterUiState.Success -> {
                navController.popBackStack()
                authViewModel.resetRegisterState()
            }
            is RegisterUiState.Error -> {
                errorMessage = (registerState as RegisterUiState.Error).message
            }
            else -> {}
        }
    }

    // Brand Colors (100% Preserved)
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val lightGray = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val errorRed = MaterialTheme.colorScheme.error

    val isLoading = registerState is RegisterUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brandGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhite)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Join Vistara for park safety",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = lightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("Enter your full name") },
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = brandGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("Enter your email") },
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = brandGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // ID Number
                    OutlinedTextField(
                        value = nationalIdNo,
                        onValueChange = { nationalIdNo = it },
                        label = { Text("ID Number") },
                        placeholder = { Text("National ID or Passport") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g., +254700000000") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null, tint = brandGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // Emergency Contact Name
                    OutlinedTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        label = { Text("Emergency Contact Name") },
                        placeholder = { Text("Name of emergency contact") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // Emergency Contact Phone
                    OutlinedTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        placeholder = { Text("Emergency phone number") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null, tint = brandGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Create a password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = brandGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Re-enter your password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = brandGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(brandGreen)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = errorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Register Button
                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() -> errorMessage = "Please enter your full name"
                                email.isBlank() -> errorMessage = "Please enter your email"
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    errorMessage = "Please enter a valid email address"
                                nationalIdNo.isBlank() -> errorMessage = "Please enter your ID number"
                                phoneNumber.isBlank() -> errorMessage = "Please enter your phone number"
                                emergencyContactName.isBlank() -> errorMessage = "Please enter emergency contact name"
                                emergencyContactPhone.isBlank() -> errorMessage = "Please enter emergency contact phone"
                                password.isBlank() -> errorMessage = "Please create a password"
                                password != confirmPassword -> errorMessage = "Passwords do not match"
                                password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                                else -> {
                                    errorMessage = null

                                    // Sending inputs to ViewModel safely
                                    contactViewModel.onEvent(ContactEvent.SetFullName(fullName))
                                    contactViewModel.onEvent(ContactEvent.SetEmail(email))
                                    contactViewModel.onEvent(ContactEvent.SetPhoneNumber(phoneNumber))
                                    contactViewModel.onEvent(ContactEvent.SetIdNumber(nationalIdNo))
                                    contactViewModel.onEvent(ContactEvent.SetEmergencyNumber(emergencyContactPhone))
                                    contactViewModel.onEvent(ContactEvent.SaveContact)

                                    authViewModel.registerUser(
                                        email = email,
                                        password = password,
                                        fullName = fullName,
                                        phoneNumber = phoneNumber,
                                        nationalId = nationalIdNo,
                                        emergencyContactName = emergencyContactName,
                                        emergencyContactPhone = emergencyContactPhone
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brandGreen,
                            disabledContainerColor = brandGreen.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "REGISTER",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already have an account? Log in",
                            color = brandGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun textFieldColors(brandGreen: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = brandGreen,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = brandGreen,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)

// Helper extension function to securely unwrap Context into a ComponentActivity
private fun Context.findActivity(): ComponentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}