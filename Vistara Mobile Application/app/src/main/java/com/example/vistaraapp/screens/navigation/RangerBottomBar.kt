package com.example.vistaraapp.screens.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RangerBottomBar(
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    val brandGreen = Color(0xFF029602)

    // windowInsets = WindowInsets(0) lets us control vertical positioning manually
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(64.dp),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0.dp)
    ) {
        // 1. DASHBOARD TAB
        NavigationBarItem(
            selected = currentRoute == "Dashboard",
            onClick = { onItemSelected("Dashboard") },
            modifier = Modifier.padding(top = 14.dp),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Dashboard",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )

        // 2. SCANNER TAB
        NavigationBarItem(
            selected = currentRoute == "Scanner",
            onClick = { onItemSelected("Scanner") },
            modifier = Modifier.padding(top = 14.dp),
            icon = {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scanner",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Scanner", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )

        // 3. ALERTS TAB
        NavigationBarItem(
            selected = currentRoute == "Alerts",
            onClick = { onItemSelected("Alerts") },
            modifier = Modifier.padding(top = 14.dp),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Alerts",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Alerts", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )

        // 4. PROFILE TAB
        NavigationBarItem(
            selected = currentRoute == "Profile",
            onClick = { onItemSelected("Profile") },
            modifier = Modifier.padding(top = 14.dp),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
    }
}
