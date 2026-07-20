package com.example.vistaraapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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

// 5. KINGFISHER PICNIC SITE CARD
@Composable
fun PicnicSiteCard() {
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
                Text("Kingfisher Picnic Site", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF029602))
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFEF3C7)) {
                    Text("Popular", fontSize = 10.sp, color = Color(0xFFD97706), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Perfect spot for a picnic with family. Enjoy the stunning views of the savanna and wildlife.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip("2.5 km from Main Gate")
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(if (isSystemInDarkTheme()) Color(0xFF2C2C2C) else Color(0xFFF3F4F6), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
