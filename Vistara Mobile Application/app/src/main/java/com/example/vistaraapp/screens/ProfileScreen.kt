package com.example.vistaraapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.database.ContactEvent
import com.example.vistaraapp.database.ContactState
import com.example.vistaraapp.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    state: ContactState,
    onEvent: (ContactEvent) -> Unit,
    authToken: String,
    onLogout: () -> Unit
) {
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val lightGrayLabel = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var isSyncing by remember { mutableStateOf(false) }

    // Fetch profile
    LaunchedEffect(authToken) {
        if (authToken.isNotEmpty()) {
            try {
                isSyncing = true

                val response = withContext(Dispatchers.IO) {
                    val bearerToken = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                    RetrofitClient.profileInstance.getProfileDetails(bearerToken)
                }

                if (response.isSuccessful) {
                    val rawBody = response.body() as? Map<*, *>
                    val dataMap = rawBody?.get("data") as? Map<*, *>

                    dataMap?.let {
                        onEvent(ContactEvent.SetFullName((it["fullName"] ?: "").toString()))
                        onEvent(ContactEvent.SetEmail((it["email"] ?: "").toString()))
                        onEvent(ContactEvent.SetPhoneNumber((it["phoneNumber"] ?: "").toString()))
                        onEvent(ContactEvent.SetEmergencyNumber((it["emergencyContactPhone"] ?: "").toString()))
                        onEvent(ContactEvent.SaveContact)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
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
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = brandGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Avatar
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(brandGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(pureWhite, CircleShape)
                        .clickable { navController.navigate("edit_profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = brandGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.fullName.ifEmpty { "Loading..." },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = brandGreen
            )


            Spacer(modifier = Modifier.height(20.dp))

            // Custom Colored Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ProfileRowItem("Full Name", state.fullName, lightGrayLabel)
                    HorizontalDivider(color=dividerColor)

                    ProfileRowItem("Email", state.email, lightGrayLabel)
                    HorizontalDivider(color=dividerColor)

                    ProfileRowItem("Phone", state.phoneNumber, lightGrayLabel)
                    HorizontalDivider(color=dividerColor)

                    ProfileRowItem("Emergency", state.emergencyNumber, lightGrayLabel)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // RESET PASSWORD
            Text(
                text = "Reset Password",
                color = brandGreen,
                modifier = Modifier
                    .clickable { navController.navigate("reset_password") }
                    .padding(8.dp)
            )

            // LOGOUT BUTTON
            Button(
                onClick = {
                    onEvent(
                        ContactEvent.Logout(
                            sessionManager = sessionManager,
                            onLogoutComplete = {
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                onLogout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileRowItem(label: String, value: String, labelColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = labelColor)
        Text(value, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
        )

    }
}