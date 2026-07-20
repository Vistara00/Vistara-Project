package com.example.vistaraapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 2. STATS ROW
@Composable
fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(value = "117 km²", label = "Park Area", color = Color(0xFF029602), modifier = Modifier.weight(1f))
        StatCard(value = "7 km", label = "From CBD", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
        StatCard(value = "100+", label = "Mammals", color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
        StatCard(value = "400+", label = "Birds", color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}
