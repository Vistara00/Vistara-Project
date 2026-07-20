package com.example.vistaraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.vistaraapp.database.ContactDatabase
import com.example.vistaraapp.database.ContactDao
import com.example.vistaraapp.repositories.BookingRepository
import com.example.vistaraapp.screens.navigation.AppNavigation
import com.example.vistaraapp.screens.navigation.ModernBottomBar
import com.example.vistaraapp.ui.theme.VistaraTheme
import com.example.vistaraapp.utils.TokenManager
import com.example.vistaraapp.viewmodels.BookingViewModel
import com.example.vistaraapp.viewmodels.SessionViewModel
import com.example.vistaraapp.viewmodels.ViewModelFactory
import com.example.vistaraapp.database.ContactViewModel

class MainActivity : ComponentActivity() {

    private val db by lazy {
        ContactDatabase.getDatabase(applicationContext)
    }
    private val sessionViewModel by viewModels<SessionViewModel>(
        factoryProducer = { ViewModelFactory(application) }
    )
    // 1. Dependency injection for Contacts
    private val contactViewModel by viewModels<ContactViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ContactViewModel(db.dao) as T
                }
            }
        }
    )

    // DEPENDENCY INJECTION FOR BOOKING/SOS VIEWMODEL
    private val bookingViewModel by viewModels<BookingViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = BookingRepository(com.example.vistaraapp.api.RetrofitClient.bookingInstance)
                    return BookingViewModel(repository) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(applicationContext)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            VistaraTheme {
                // BOOKING VIEWMODEL INTO VISTARAAPP
                VistaraApp(
                    contactViewModel = contactViewModel,
                    bookingViewModel = bookingViewModel,
                    sessionViewModel = sessionViewModel,
                    contactDao = db.dao
                )
            }
        }
    }
}

@Composable
fun VistaraApp(
    contactViewModel: ContactViewModel,
    bookingViewModel: BookingViewModel,
    sessionViewModel: SessionViewModel,
    contactDao: ContactDao
) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(TokenManager.getToken()?.isNotEmpty() == true) }

    var sessionToken by remember { mutableStateOf(TokenManager.getToken() ?: "") }
    val contactState by contactViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            if (isLoggedIn) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"
                val showBottomBar = currentRoute in listOf("home", "wildlife", "bookings", "profile")

                if (showBottomBar) {
                    ModernBottomBar(
                        currentRoute = currentRoute,
                        onItemSelected = { route ->
                            if (route == "bookings") {
                                bookingViewModel.fetchBookings(sessionToken)
                            }
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            contactViewModel = contactViewModel,
            bookingViewModel = bookingViewModel, // Safely moving into AppNavigation graph
            contactState = contactState,
            contactDao = contactDao,
            sessionViewModel = sessionViewModel,
            sessionToken = sessionToken,

            onTokenUpdated = { newTokenString ->
                sessionToken = newTokenString
                TokenManager.saveToken(newTokenString)
                if (newTokenString.isEmpty()) {
                    isLoggedIn = false
                }
            },

            onLoginSuccess = { isLoggedIn = true },
            modifier = Modifier.padding(innerPadding)
        )
    }
}