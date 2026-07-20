package com.example.vistaraapp.screens.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Home",
        icon = Icons.Filled.Home,
        selectedIcon = Icons.Filled.Home
    )

    object Explore : BottomNavItem(
        route = "wildlife",
        title = "Explore",
        icon = Icons.Default.Search,
        selectedIcon = Icons.Default.Search
    )

    object Bookings : BottomNavItem(
        route = "bookings",
        title = "Bookings",
        icon = Icons.Filled.Info,
        selectedIcon = Icons.Filled.Info
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        icon = Icons.Filled.Person,
        selectedIcon = Icons.Filled.Person
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Explore,
    BottomNavItem.Bookings,
    BottomNavItem.Profile
)