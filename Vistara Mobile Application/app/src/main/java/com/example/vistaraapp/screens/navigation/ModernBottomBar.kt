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
fun ModernBottomBar(
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    val brandGreen = Color(0xFF029602)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        // 1. HOME TAB
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onItemSelected("home") },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Home", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent // Clean look, no dark capsule pill!
            )
        )

        //  2. EXPLORE TAB
        NavigationBarItem(
            selected = currentRoute == "wildlife",
            onClick = { onItemSelected("wildlife") },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Explore",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Explore", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandGreen,
                selectedTextColor = brandGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )

        // 3. BOOKINGS TAB
        NavigationBarItem(
            selected = currentRoute == "bookings",
            onClick = { onItemSelected("bookings") },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Bookings",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(text = "Bookings", fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
            selected = currentRoute == "profile",
            onClick = { onItemSelected("profile") },
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