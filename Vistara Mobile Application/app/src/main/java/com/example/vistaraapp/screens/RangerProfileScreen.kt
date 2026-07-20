package com.example.vistaraapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.RangerProfileViewModel

@Composable
fun RangerProfileScreen(
    viewModel: RangerProfileViewModel,
    onLogoutSuccess: () -> Unit,
    onResetPasswordClick: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()

    val brandGreen = Color(0xFF029602)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val lightGrayLabel = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = brandGreen
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Box (110.dp size to match Visitor Profile)
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = state.fullName.ifEmpty { "Ranger" },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = brandGreen
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Details Card matching Visitor Profile
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
                RangerProfileRowItem("Email", state.email, lightGrayLabel)
                HorizontalDivider(color = dividerColor)
                RangerProfileRowItem("Role", state.role, lightGrayLabel)
            }
        }

        Spacer(modifier = Modifier.weight(1f))



        // LOGOUT BUTTON
        Button(
            onClick = {
                viewModel.logout(onLogoutSuccess)
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

@Composable
private fun RangerProfileRowItem(label: String, value: String, labelColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = labelColor)
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}