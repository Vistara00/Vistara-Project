package com.example.vistaraapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.AlertsViewModel
import com.example.vistaraapp.data.SessionManager
import com.example.vistaraapp.entities_dataclass.RangerSosAlert
import kotlinx.coroutines.launch


@Composable
fun RangerAlertsContent(
    alertsViewModel: AlertsViewModel,
    sessionManager: SessionManager,
    sosAlerts: List<RangerSosAlert> = emptyList(),
    onUpdateAlert: (RangerSosAlert) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()

    // API alerts state
    val apiAlerts by alertsViewModel.alerts.collectAsState()
    val isLoadingApi by alertsViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        val token = sessionManager.getToken() ?: ""
        if (token.isNotEmpty()) {
            alertsViewModel.loadAllAlerts(token)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Header Section
        item {
            Text(
                text = "Alerts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF029602),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Filter Chips Section
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("All", "PENDING", "RESPONDING", "RESOLVED").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val chipBg = if (isSelected) Color(0xFF029602) else MaterialTheme.colorScheme.surfaceVariant
                    val chipText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBg)
                            .clickable {
                                selectedFilter = filter
                                scope.launch {
                                    val token = sessionManager.getToken() ?: ""
                                    if (token.isNotEmpty()) {
                                        when (filter) {
                                            "All" -> alertsViewModel.loadAllAlerts(token)
                                            "PENDING" -> alertsViewModel.fetchPendingEmergencies(token)
                                            else -> alertsViewModel.fetchAlertsByStatus(token, filter)
                                        }
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = chipText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // List Content Section (100% powered by live backend API alerts)
        if (isLoadingApi) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF029602))
                }
            }
        } else if (apiAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No alerts found on server for: $selectedFilter",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(apiAlerts) { alert ->
                RangerAlertCard(
                    alert = alert,
                    selectedFilter = selectedFilter,
                    onClaimClick = {
                        scope.launch {
                            val token = sessionManager.getToken() ?: ""
                            if (token.isNotEmpty()) {
                                alertsViewModel.claimAlert(token, alert.id) {
                                    when (selectedFilter) {
                                        "All" -> alertsViewModel.loadAllAlerts(token)
                                        "PENDING" -> alertsViewModel.fetchPendingEmergencies(token)
                                        else -> alertsViewModel.fetchAlertsByStatus(token, selectedFilter)
                                    }
                                    alertsViewModel.loadRangerStats(token)
                                }
                            }
                        }
                    },
                    onResolveClick = {
                        scope.launch {
                            val token = sessionManager.getToken() ?: ""
                            if (token.isNotEmpty()) {
                                alertsViewModel.resolveAlert(
                                    token = token,
                                    alertId = alert.id,
                                    notes = "Resolved by Ranger from Mobile App"
                                ) {
                                    when (selectedFilter) {
                                        "All" -> alertsViewModel.loadAllAlerts(token)
                                        "PENDING" -> alertsViewModel.fetchPendingEmergencies(token)
                                        else -> alertsViewModel.fetchAlertsByStatus(token, selectedFilter)
                                    }
                                    alertsViewModel.loadRangerStats(token)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}