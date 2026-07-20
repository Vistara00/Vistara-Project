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
import com.example.vistaraapp.entities_dataclass.NotificationItem
import com.example.vistaraapp.ui.components.NotificationCard
import com.example.vistaraapp.ui.components.SectionHeader
import com.example.vistaraapp.utils.isBookingNotification
import com.example.vistaraapp.utils.isBroadcastNotification
import com.example.vistaraapp.viewmodels.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastNotificationsListScreen(
    navController: NavController,
    viewModel: BookingViewModel,
    authToken: String
) {
    val brandGreen = Color(0xFF029602)
    val liveNotifications by viewModel.notifications.collectAsState()
    val unreadNotifications by viewModel.unreadNotifications.collectAsState()
    val isLoadingAll by viewModel.isLoadingNotifications.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = authToken) {
        if (authToken.isNotEmpty() && authToken != "OFFLINE_SESSION") {
            viewModel.fetchAllNotifications(authToken)
            viewModel.fetchUnreadNotifications(authToken)
        }
    }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val topBarBg = if (isDark) Color(0xFF1C1C1E) else Color.White

    // Filter broadcast notifications only
    val broadcastNotifications = liveNotifications.filter { isBroadcastNotification(it) && !isBookingNotification(it) }
    val unreadBroadcastList = unreadNotifications.filter { isBroadcastNotification(it) && !isBookingNotification(it) }
    val readBroadcastList = broadcastNotifications.filter { unreadBroadcastList.none { unread -> unread.id == it.id } }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Broadcasts", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else Color.Black
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = brandGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topBarBg)
            )
        }
    ) { innerPadding ->

        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchAllNotifications(authToken)
                viewModel.fetchUnreadNotifications(authToken)
                isRefreshing = false
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(bgColor)) {

                // Unread Section
                if (unreadBroadcastList.isNotEmpty()) {
                    item { SectionHeader("New Notifications") }
                    items(unreadBroadcastList) { notification ->
                        NotificationCard(notification) {
                            viewModel.markNotificationAsRead(authToken, notification.id)
                            navController.navigate("notification_detail/${notification.id}")
                        }
                    }
                }

                // Read Section
                if (readBroadcastList.isNotEmpty()) {
                    item { SectionHeader("Earlier") }
                    items(readBroadcastList) { notification ->
                        NotificationCard(notification) {
                            navController.navigate("notification_detail/${notification.id}")
                        }
                    }
                }

                if (broadcastNotifications.isEmpty() && !isLoadingAll) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No broadcast notifications found", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
