package com.example.vistaraapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.vistaraapp.viewmodels.BookingViewModel
import com.example.vistaraapp.viewmodels.WeatherState
import com.example.vistaraapp.viewmodels.WeatherViewModel
import com.example.vistaraapp.ui.theme.VistaraTheme
import com.example.vistaraapp.viewmodel.SosViewModel
import com.example.vistaraapp.viewmodels.SessionViewModel
import java.util.Calendar

// DYNAMIC CONTENT HELPERS
fun getDynamicGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

fun getDynamicBoldPhrase(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Nairobi is Waking Up!"
        in 12..16 -> "The Savanna Awaits You!"
        else -> "Unwind in the Wild Tonight!"
    }
}

// MAIN HOME SCREEN ENTRIES
@Composable
fun HomeScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel,
    viewModel: BookingViewModel,
    sessionViewModel: SessionViewModel,
    sosViewModel: SosViewModel,
    authToken: String
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val context = LocalContext.current
    val sosStatus by viewModel.sosStatus
    val sosMessage = sosViewModel.sosMessage
    val unreadCount by viewModel.unreadCount.collectAsState()

    val sessionUiState by sessionViewModel.uiState.collectAsState()
    val checkInState by sessionViewModel.checkInState.collectAsState()
    val isCheckedIn = sessionUiState is com.example.vistaraapp.viewmodels.SessionUiState.Success ||
            checkInState is com.example.vistaraapp.viewmodels.SessionUiState.Success

    // Background pre-fetch engine: keeps notification items hot and responsive
    LaunchedEffect(key1 = authToken) {
        if (authToken.isNotEmpty() && authToken != "OFFLINE_SESSION") {
            viewModel.fetchAllNotifications(authToken)
            viewModel.fetchUnreadNotificationsCount(authToken)
            sessionViewModel.checkCurrentSession(authToken)
        }
    }

    LaunchedEffect(sosStatus) {
        sosStatus?.let { statusMessage ->
            Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
            viewModel.clearSosStatus()
        }
    }

    LaunchedEffect(sosMessage) {
        sosMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    HomeScreenContent(
        navController = navController,
        weatherState = weatherState,
        unreadCount = unreadCount,
        isCheckedIn = isCheckedIn,
        onRetryWeather = { weatherViewModel.fetchWeather() },
        onSendEmergencyReport = { emergencyType, details ->
            val mappedType = when (emergencyType.trim()) {
                "Wildlife Encounter" -> "WILDLIFE_ENCOUNTER"
                "Medical" -> "MEDICAL"
                "Accident" -> "ACCIDENT"
                "Lost" -> "LOST"
                "General Distress" -> "GENERAL_DISTRESS"
                "Vehicle Breakdown" -> "VEHICLE_BREAKDOWN"
                else -> "GENERAL_DISTRESS"
            }

            // Utilizing sosViewModel to automatically track and dispatch the real-time hardware location
            Log.d("HomeScreen", "Triggering SOS via sosViewModel for type: $mappedType")
            sosViewModel.triggerEmergencySos(
                context = context,
                alertType = mappedType,
                message = "Emergency Type: $emergencyType. Details: $details"
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    navController: NavController,
    weatherState: WeatherState,
    unreadCount: Int,
    isCheckedIn: Boolean,
    onRetryWeather: () -> Unit,
    onSendEmergencyReport: (type: String, details: String) -> Unit
) {
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val lightGray = MaterialTheme.colorScheme.background
    val emergencyRed = Color(0xFFD32F2F)

    var showEmergencyOverlay by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(pureWhite)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Vistara",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandGreen
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "View Alert Notifications",
                                    tint = brandGreen
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhite),
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 28.dp,
                                bottomEnd = 28.dp
                            )
                        )
                        .drawBehind {
                            drawRect(color = pureWhite)
                        }
                )
            }
        },
        floatingActionButton = {
            if (isCheckedIn) {
                FloatingActionButton(
                    onClick = { showEmergencyOverlay = true },
                    containerColor = emergencyRed,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Emergency",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = lightGray
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { HeroDashboardCard(weatherState) }
                item { StatsRow() }
                item { RealTimeWeatherCard(brandGreen, weatherState, onRetryWeather) }
                item { LiveMapCard(navController, brandGreen) }
                item { WildlifeDiscoveryCard(navController, brandGreen) }
                item { PicnicSiteCard() }
            }

            if (showEmergencyOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { showEmergencyOverlay = false },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .clickable(enabled = false) { }
                    ) {
                        // calls emergency info card
                        EmergencyInfoCard(
                            onSendEmergencyReport = { type, details ->
                                onSendEmergencyReport(type, details)
                                showEmergencyOverlay = false
                            },
                            brandGreen = brandGreen
                        )
                    }
                }
            }
        }
    }
}

// Preview setup
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    VistaraTheme {
        val dummyNavController = rememberNavController()

        HomeScreenContent(
            navController = dummyNavController,
            weatherState = WeatherState.Loading,
            unreadCount = 0,
            isCheckedIn = true,
            onRetryWeather = {},
            onSendEmergencyReport = { _, _ -> }
        )
    }
}