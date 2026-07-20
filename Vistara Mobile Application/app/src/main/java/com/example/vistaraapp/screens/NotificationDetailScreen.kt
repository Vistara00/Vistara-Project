package com.example.vistaraapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.entities_dataclass.NotificationItem
import com.example.vistaraapp.viewmodels.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    navController: NavController,
    notificationId: String,
    viewModel: BookingViewModel,
    authToken: String
) {
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.background
    val topAppBarColor = MaterialTheme.colorScheme.surface

    val liveNotifications by viewModel.notifications.collectAsState()
    val unreadNotifications by viewModel.unreadNotifications.collectAsState()

    // Find the matching notification
    val notification = remember(notificationId, liveNotifications, unreadNotifications) {
        liveNotifications.find { it.id == notificationId }
            ?: unreadNotifications.find { it.id == notificationId }
    }

    LaunchedEffect(key1 = authToken) {
        if (authToken.isNotEmpty() && authToken != "OFFLINE_SESSION") {
            if (viewModel.notifications.value.isEmpty()) {
                viewModel.fetchAllNotifications(authToken)
            }
            if (viewModel.unreadNotifications.value.isEmpty()) {
                viewModel.fetchUnreadNotifications(authToken)
            }
        }
    }

    Scaffold(
        containerColor = pureWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notification Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brandGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topAppBarColor)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(pureWhite)
        ) {
            if (notification == null) {
                // If it is loading or not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = brandGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading notification details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual type icon badge
                    val iconColor = if (notification.isAlert) Color(0xFFD32F2F) else brandGreen
                    val iconBg = iconColor.copy(alpha = 0.1f)
                    val iconVector = when (notification.type.lowercase()) {
                        "alert", "warning", "emergency" -> Icons.Default.Warning
                        "info" -> Icons.Default.Info
                        else -> Icons.Default.Notifications
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(color = iconBg, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = notification.type,
                            tint = iconColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Type label
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = notification.type.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = iconColor
                        )
                    }

                    // Notification Card Content
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = notification.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = notification.timestamp,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = notification.message,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }


                }
            }
        }
    }
}
