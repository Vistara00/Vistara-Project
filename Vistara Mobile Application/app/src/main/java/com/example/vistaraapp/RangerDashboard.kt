package com.example.vistaraapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.*
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.data.SessionManager
import com.example.vistaraapp.database.ContactDatabase
import com.example.vistaraapp.repositories.AuthRepository
import com.example.vistaraapp.repositories.QrScannerRepository
import com.example.vistaraapp.screens.RangerAlertsContent
import com.example.vistaraapp.screens.RangerHomeContent
import com.example.vistaraapp.screens.RangerProfileScreen
import com.example.vistaraapp.screens.navigation.RangerBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangerDashboard(
    onLogoutSuccess: () -> Unit,
    onResetPasswordClick: (() -> Unit)? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate("Scanner") {
                launchSingleTop = true
            }
        }
    }

    val contactDao = remember { ContactDatabase.getDatabase(context).dao }
    val sessionManager = remember { SessionManager(context) }
    val rangerViewModel: RangerProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RangerProfileViewModel(
                    authRepository = AuthRepository(contactDao),
                    sessionManager = sessionManager
                )
            }
        }
    )

    val alertsViewModel: AlertsViewModel = viewModel()

    // QR Scanner ViewModel
    val qrScannerViewModel: QrScannerViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                QrScannerViewModel(QrScannerRepository(RetrofitClient.bookingInstance))
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vistara Ranger",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF029602)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 28.dp,
                        bottomEnd = 28.dp
                    )
                )
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "Dashboard"

            RangerBottomBar(
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    if (route == "Scanner") {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            navController.navigate("Scanner") {
                                launchSingleTop = true
                            }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    } else {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "Dashboard", Modifier.padding(innerPadding)) {
            composable("Dashboard") {
                RangerHomeContent(
                    alertsViewModel = alertsViewModel,
                    sessionManager = sessionManager
                )
            }
            composable("Scanner") {
                var token by remember { mutableStateOf("") }
                LaunchedEffect(Unit) {
                    token = sessionManager.getToken() ?: ""
                }
                com.example.vistaraapp.screens.RangerScannerScreen(
                    qrScannerViewModel = qrScannerViewModel,
                    token = token
                )
            }
            composable("Alerts") {
                LaunchedEffect(Unit) {
                    val token = sessionManager.getToken() ?: ""
                    if (token.isNotEmpty()) {
                        alertsViewModel.loadAllAlerts(token)
                    }
                }

                RangerAlertsContent(
                    alertsViewModel = alertsViewModel,
                    sessionManager = sessionManager
                )
            }
            composable("Profile") {
                RangerProfileScreen(
                    viewModel = rangerViewModel,
                    onLogoutSuccess = onLogoutSuccess,
                    onResetPasswordClick = onResetPasswordClick
                )
            }
        }
    }
}