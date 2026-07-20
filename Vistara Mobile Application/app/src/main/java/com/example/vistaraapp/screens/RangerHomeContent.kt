package com.example.vistaraapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
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
import com.example.vistaraapp.RangerAlert
import com.example.vistaraapp.data.SessionManager
import com.example.vistaraapp.entities_dataclass.RangerSosAlert
import kotlinx.coroutines.launch


private fun alertStatusColor(status: String?): Color = when (status?.uppercase()) {
    "PENDING", "UNRESOLVED" -> Color(0xFFE53935)
    "RESPONDING", "ASSIGNED" -> Color(0xFFFFB300)
    "RESOLVED" -> Color(0xFF029602)
    else -> Color(0xFF2196F3)
}


private fun priorityColor(priority: String?): Color = when (priority?.uppercase()) {
    "HIGH"   -> Color(0xFFE53935)
    "MEDIUM" -> Color(0xFFFFB300)
    "LOW"    -> Color(0xFF029602)
    else     -> Color(0xFF9E9E9E)
}

private fun formatTimestamp(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        // "2025-07-09T08:14:07.123" → "09 Jul, 08:14"
        val parts = iso.substringBefore('.').split("T")
        val dateParts = parts[0].split("-")
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec")
        val month = months.getOrElse(dateParts[1].toInt() - 1) { "" }
        "${dateParts[2]} $month, ${parts.getOrElse(1) { "" }.take(5)}"
    } catch (_: Exception) { iso.take(16) }
}

// Rich alert card
@Composable
fun RangerAlertCard(
    alert: RangerAlert,
    selectedFilter: String,
    onClaimClick: () -> Unit,
    onResolveClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val statusColor = alertStatusColor(alert.alertStatus)
    val priorityCol = priorityColor(alert.priority)

    // card background tinted by status
    val cardBg = when (alert.alertStatus?.uppercase()) {
        "PENDING", "UNRESOLVED" -> if (isDark) Color(0xFF3A1A1A) else Color(0xFFFFF3F3)
        "RESPONDING", "ASSIGNED" -> if (isDark) Color(0xFF3E3622) else Color(0xFFFFFDE7)
        "RESOLVED" -> if (isDark) Color(0xFF1A2E1A) else Color(0xFFF1F8F1)
        else -> if (isDark) Color(0xFF1E2A3A) else Color(0xFFE8F4FD)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row 1: alert type + status + priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alert type label
                Text(
                    text = alert.alertType
                        ?.replace("_", " ")
                        ?.replaceFirstChar { it.uppercaseChar() }
                        ?: "Emergency",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Priority chip
                    if (!alert.priority.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = priorityCol.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = alert.priority.uppercase(),
                                color = priorityCol,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = alert.alertStatus?.uppercase() ?: selectedFilter,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Row 2: message
            Spacer(Modifier.height(8.dp))
            Text(
                text = alert.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Row 3: visitor info
            if (!alert.visitorName.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = alert.visitorName ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Row 4: phone
            if (!alert.visitorPhone.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = alert.visitorPhone ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Row 5: location coords
            if (alert.latitude != null && alert.longitude != null && (alert.latitude != 0.0 || alert.longitude != 0.0)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "%.5f, %.5f".format(alert.latitude, alert.longitude),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Row 6: assigned ranger
            if (!alert.assignedRangerName.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFF029602),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Assigned to: ${alert.assignedRangerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Row 7: timestamps
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = buildString {
                        append("Reported: ${formatTimestamp(alert.createdAt)}")
                        if (!alert.respondedAt.isNullOrBlank())
                            append("  •  Responded: ${formatTimestamp(alert.respondedAt)}")
                        if (!alert.resolvedAt.isNullOrBlank())
                            append("  •  Resolved: ${formatTimestamp(alert.resolvedAt)}")
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            val statusUpper = alert.alertStatus?.uppercase() ?: ""
            val showClaim = selectedFilter == "PENDING" || (selectedFilter == "All" && statusUpper in listOf("PENDING", "UNRESOLVED") && alert.assignedRangerName.isNullOrBlank())
            val showResolve = selectedFilter == "RESPONDING" || (selectedFilter == "All" && statusUpper in listOf("RESPONDING", "ASSIGNED"))

            if (showClaim || showResolve) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = if (showClaim) onClaimClick else onResolveClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF029602)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showResolve) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = if (showClaim) "Assign to Me" else "Mark Resolved",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// RangerHomeContent
@Composable
fun RangerHomeContent(
    alertsViewModel: AlertsViewModel,
    sessionManager: SessionManager,
    sosAlerts: List<RangerSosAlert> = emptyList(),
    onUpdateAlert: (RangerSosAlert) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()

    val apiAlerts by alertsViewModel.alerts.collectAsState()
    val isLoadingApi by alertsViewModel.isLoading.collectAsState()
    val stats by alertsViewModel.stats.collectAsState()

    LaunchedEffect(Unit) {
        val token = sessionManager.getToken() ?: ""
        android.util.Log.d("RangerHomeContent", "LaunchedEffect: token='$token'")
        if (token.isNotEmpty()) {
            alertsViewModel.loadRangerStats(token)
            alertsViewModel.loadAllAlerts(token)
        }
    }

    val unresolvedCount = remember(apiAlerts, stats) {
        stats?.totalPendingAlerts ?: apiAlerts.count { it.alertStatus?.uppercase() in listOf("PENDING", "UNRESOLVED") }
    }
    val assignedCount = remember(apiAlerts, stats) {
        stats?.totalAssigned ?: apiAlerts.count { it.alertStatus?.uppercase() in listOf("ASSIGNED", "RESPONDING") }
    }
    val respondingCount = remember(apiAlerts, stats) {
        stats?.assignedResponding ?: apiAlerts.count { it.alertStatus?.uppercase() == "RESPONDING" }
    }
    val activeSosCount = remember(stats, unresolvedCount, assignedCount) {
        stats?.let { it.totalPendingAlerts + it.totalAssigned } ?: (unresolvedCount + assignedCount)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcome header
        item {
            Column {
                Text("Welcome back,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Officer Ranger", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        // 2×2 Stats grid
        item {
            RangerStatCardsGrid(
                activeSosCount = activeSosCount,
                assignedCount  = assignedCount,
                unresolvedCount = unresolvedCount,
                respondingCount = respondingCount
            )
        }

        // Section header + filter chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SOS Alerts Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF029602)
                )
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "PENDING", "RESPONDING", "RESOLVED").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF029602) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    selectedFilter = filter
                                    scope.launch {
                                        val token = sessionManager.getToken() ?: ""
                                        if (token.isNotEmpty()) {
                                            when (filter) {
                                                "All"     -> alertsViewModel.loadAllAlerts(token)
                                                "PENDING" -> alertsViewModel.fetchPendingEmergencies(token)
                                                else      -> alertsViewModel.fetchAlertsByStatus(token, filter)
                                            }
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Alert list
        if (isLoadingApi) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFF029602)) }
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
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No alerts found on server", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
