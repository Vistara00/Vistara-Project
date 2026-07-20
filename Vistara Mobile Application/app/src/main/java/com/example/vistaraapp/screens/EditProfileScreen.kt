package com.example.vistaraapp.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.database.ContactEvent
import com.example.vistaraapp.database.ContactState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    state: ContactState,
    onEvent: (ContactEvent) -> Unit,
    // Add email to the callback structure so it can be sent to the cloud backend
    onSaveProfileApi: (fullName: String, phone: String, email: String, emergencyPhone: String) -> Unit
) {
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val lightGray = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFF6F6F6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(16.dp))

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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Full Name Input Field
                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = { onEvent(ContactEvent.SetFullName(it)) },
                        label = { Text("Full Name") },
                        placeholder = { Text("Enter your Full Name", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    // Phone Number Input Field
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = { onEvent(ContactEvent.SetPhoneNumber(it)) },
                        label = { Text("Phone Number") },
                        placeholder = { Text("Enter your Phone Number", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    // Emergency Contact Input Field
                    OutlinedTextField(
                        value = state.emergencyNumber,
                        onValueChange = { onEvent(ContactEvent.SetEmergencyNumber(it)) },
                        label = { Text("Emergency Contact No") },
                        placeholder = { Text("Enter Emergency Contact No", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    // SAVE Button
                    Button(
                        onClick = {
                            // 1. Save to local Room database profile row
                            onEvent(ContactEvent.SaveContact)

                            // 2. Pass data to the cloud backend API function
                            // We securely pass state.email behind the scenes so the
                            // cloud database knows whose record to update without showing an email field!
                            onSaveProfileApi(
                                state.fullName,
                                state.phoneNumber,
                                state.email,
                                state.emergencyNumber
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brandGreen,
                            disabledContainerColor = brandGreen.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "SAVE PROFILE DETAILS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}