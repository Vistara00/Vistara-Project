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
import androidx.navigation.NavController
import com.example.vistaraapp.MapButton

// 3b. LIVE MAP CARD
@Composable
fun LiveMapCard(navController: NavController, brandGreen: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Park Tracking & Map", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandGreen)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = brandGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        color = brandGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Track your live location inside Nairobi National Park, view landmarks, and stay safe with real-time tracking.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(16.dp))
            MapButton(
                navController = navController,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
