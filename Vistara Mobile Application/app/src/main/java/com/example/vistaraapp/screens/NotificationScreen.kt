package com.example.vistaraapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.ui.components.BookingInboxEntry
import com.example.vistaraapp.ui.components.BroadcastInboxEntry
import com.example.vistaraapp.ui.components.OtherNotificationRow
import com.example.vistaraapp.utils.isBookingNotification
import com.example.vistaraapp.utils.isBroadcastNotification
import com.example.vistaraapp.viewmodels.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: BookingViewModel,
    authToken: String
) {
    // 1. DYNAMIC THEME COLORS
    val isDark = isSystemInDarkTheme()
    val brandGreen = Color(0xFF029602)
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDark) Color(0xFF1C1C1E) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val secondaryTextColor = Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    val liveNotifications by viewModel.notifications.collectAsState()
    val unreadNotifications by viewModel.unreadNotifications.collectAsState()
    val isLoadingAll by viewModel.isLoadingNotifications.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(authToken) {
        if (authToken.isNotEmpty() && authToken != "OFFLINE_SESSION") {
            viewModel.fetchAllNotifications(authToken)
            viewModel.fetchUnreadNotifications(authToken)
        }
    }

    // Filtering Logic
    val unreadBookings = remember(unreadNotifications) { unreadNotifications.filter { isBookingNotification(it) } }
    val readBookings = remember(liveNotifications, unreadNotifications) {
        liveNotifications.filter { isBookingNotification(it) && unreadNotifications.none { u -> u.id == it.id } }
    }
    val unreadBroadcasts = remember(unreadNotifications) { unreadNotifications.filter { isBroadcastNotification(it) && !isBookingNotification(it) } }
    val readBroadcasts = remember(liveNotifications, unreadNotifications) {
        liveNotifications.filter { isBroadcastNotification(it) && !isBookingNotification(it) && unreadNotifications.none { u -> u.id == it.id } }
    }
    val otherNotifications = remember(liveNotifications) {
        liveNotifications.filter { !isBookingNotification(it) && !isBroadcastNotification(it) }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = brandGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bgColor)
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.fetchAllNotifications(authToken)
                viewModel.fetchUnreadNotifications(authToken)
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(bgColor)) {

                // Aggregated Bookings
                if (unreadBookings.isNotEmpty() || readBookings.isNotEmpty()) {
                    item {
                        BookingInboxEntry(
                            unreadCount = unreadBookings.size,
                            latestMessage = unreadBookings.firstOrNull()?.message ?: readBookings.firstOrNull()?.message ?: "",
                            latestTimestamp = unreadBookings.firstOrNull()?.timestamp ?: readBookings.firstOrNull()?.timestamp ?: "",
                            onClick = {
                                if (unreadBookings.isNotEmpty()) viewModel.markMultipleNotificationsAsRead(authToken, unreadBookings.map { it.id })
                                navController.navigate("booking_notifications_list")
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    }
                }

                // Aggregated Broadcasts
                if (unreadBroadcasts.isNotEmpty() || readBroadcasts.isNotEmpty()) {
                    item {
                        BroadcastInboxEntry(
                            unreadCount = unreadBroadcasts.size,
                            latestMessage = unreadBroadcasts.firstOrNull()?.message ?: readBroadcasts.firstOrNull()?.message ?: "",
                            latestTimestamp = unreadBroadcasts.firstOrNull()?.timestamp ?: readBroadcasts.firstOrNull()?.timestamp ?: "",
                            onClick = {
                                if (unreadBroadcasts.isNotEmpty()) viewModel.markMultipleNotificationsAsRead(authToken, unreadBroadcasts.map { it.id })
                                navController.navigate("broadcast_notifications_list")
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    }
                }

                // Individual Rows
                items(otherNotifications) { notification ->
                    val isUnread = unreadNotifications.any { it.id == notification.id }
                    OtherNotificationRow(
                        notification = notification,
                        isUnread = isUnread,
                        onClick = {
                            if (isUnread) viewModel.markNotificationAsRead(authToken, notification.id)
                            navController.navigate("notification_detail/${notification.id}")
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                }

                // Empty State
                if (liveNotifications.isEmpty() && !isLoadingAll) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No notifications found", color = secondaryTextColor)
                        }
                    }
                }
            }
        }
    }
}