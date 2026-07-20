package com.example.vistaraapp.screens.navigation
import com.example.vistaraapp.screens.RangerScannerContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.ProfileNetworkRequest
import com.example.vistaraapp.data.SessionManager
import com.example.vistaraapp.utils.TokenManager
import com.example.vistaraapp.database.*
import com.example.vistaraapp.entities_dataclass.uniqueAnimals
import com.example.vistaraapp.repositories.*
import com.example.vistaraapp.screens.*
import com.example.vistaraapp.viewmodels.*
import com.example.vistaraapp.viewmodel.SosViewModel
import com.example.vistaraapp.RangerDashboard
import com.example.vistaraapp.RangerProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    bookingViewModel: BookingViewModel,
    contactState: ContactState,
    contactDao: ContactDao,
    sessionViewModel: SessionViewModel,
    sessionToken: String,
    onTokenUpdated: (String) -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val tokenState = rememberUpdatedState(sessionToken)
    val currentContactState = rememberUpdatedState(contactState)
    val coroutineScope = rememberCoroutineScope()


    // AUTOMATIC SESSION CHECK AT STARTUP (AUTO-LOGIN)
    LaunchedEffect(Unit) {
        // 1. Look up if an active profile row exists in the local Room database (ID = 1)
        val activeProfile = withContext(Dispatchers.IO) {
            contactDao.getContactById(1)
        }

        // 2. If an active session exists on this device, proceed
        if (activeProfile != null && activeProfile.isCurrentUser) {

            // 3. Read the real saved token directly from your DataStore suspension method
            val savedToken = sessionManager.getToken() ?: ""

            if (savedToken.isNotEmpty() && savedToken != "OFFLINE_SESSION") {
                // Pass the real token back up to your global app state wrapper
                onTokenUpdated(savedToken)
                onLoginSuccess()

                // Read user role and route to the correct dashboard
                var savedRole = sessionManager.getRole() ?: ""
                if (savedRole.isEmpty() && savedToken.isNotEmpty() && savedToken != "OFFLINE_SESSION") {
                    savedRole = TokenManager.decodeJwtRole(savedToken) ?: ""
                    if (savedRole.isNotEmpty()) {
                        sessionManager.saveRole(savedRole)
                    }
                }

                val destination = if (TokenManager.isRangerRole(savedRole)) {
                    "ranger_dashboard"
                } else {
                    "home"
                }

                // 4. Jump past the auth entry screens cleanly
                navController.navigate(destination) {
                    popUpTo("login") { inclusive = true } // Clear the backstack history
                }
            } else {
                // Clear active session flags to force manual login
                withContext(Dispatchers.IO) {
                    contactDao.upsertContact(activeProfile.copy(isCurrentUser = false))
                }
                TokenManager.clearToken()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {

        // LOGIN SCREEN
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val repo = AuthRepository(contactDao)
                        LoginViewModel(
                            authRepository = repo,
                            sessionManager = sessionManager
                        )
                    }
                }
            )

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToDashboard = { destination ->
                    // Read the actual auth token from the ViewModel state
                    val token = loginViewModel.state.value.token ?: ""
                    onTokenUpdated(token)
                    onLoginSuccess()

                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToResetPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        // REGISTRATION SCREEN
        composable("register") {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AuthViewModel(AuthRepository(contactDao))
                    }
                }
            )

            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel,
                contactViewModel = contactViewModel
            )
        }

        // FORGOT PASSWORD SCREEN
        composable("forgot_password") {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AuthViewModel(AuthRepository(contactDao))
                    }
                }
            )

            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // HOME MAIN DASHBOARD (Tourist / Visitor)
        composable("home") {
            val weatherViewModel: WeatherViewModel = viewModel()
            val sosViewModel: SosViewModel = viewModel()
            HomeScreen(
                navController = navController,
                weatherViewModel = weatherViewModel,
                viewModel = bookingViewModel,
                sessionViewModel = sessionViewModel,
                sosViewModel = sosViewModel,
                authToken = tokenState.value
            )
        }

        // RANGER DASHBOARD
        composable("ranger_dashboard") {
            RangerDashboard(
                onLogoutSuccess = {
                    onTokenUpdated("")
                    // Redirect to login screen and clear backstack
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                },
                onResetPasswordClick = {
                    navController.navigate("reset_password")
                }
            )
        }

        // WILDLIFE FEED
        composable("wildlife") {
            WildlifeScreen(navController)
        }

        // SAFARI BOOKINGS MANAGEMENT
        composable("bookings") {
            BookingsScreen(
                navController = navController,
                viewModel = bookingViewModel,
                authToken = tokenState.value
            )
        }

        // USER PROFILE CARD
        composable("profile") {
            ProfileScreen(
                navController = navController,
                state = currentContactState.value,
                onEvent = contactViewModel::onEvent,
                authToken = tokenState.value,
                onLogout = {
                    onTokenUpdated("")
                }
            )
        }

        //Map screen
        composable("map_screen") {
            MapScreen()
        }
        // EDIT DETAILS FORM
        composable("edit_profile") {
            EditProfileScreen(
                navController = navController,
                state = currentContactState.value,
                onEvent = contactViewModel::onEvent,
                onSaveProfileApi = { fullName, phone, _, emergencyPhone ->
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val requestPayload = ProfileNetworkRequest(
                                fullName = fullName,
                                phoneNumber = phone.replace(Regex("[^0-9]"), ""),
                                emergencyContactName = "Emergency Contact",
                                emergencyContactPhone = emergencyPhone.replace(Regex("[^0-9]"), ""),
                                nationalId = currentContactState.value.idNumber
                            )

                            val token = tokenState.value
                            val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                            val response = RetrofitClient.profileInstance.saveProfileDetails(
                                bearerToken = bearerToken,
                                profileData = requestPayload
                            )

                            if (response.isSuccessful) {
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }

        // ANIMAL SPECIFIC DETAILS
        composable("animal/{animalId}") { backStackEntry ->
            val animalId = backStackEntry.arguments
                ?.getString("animalId")
                ?.toIntOrNull() ?: 1

            val animal = uniqueAnimals.find { it.id == animalId } ?: uniqueAnimals[0]

            AnimalDetailScreen(
                animal = animal,
                navController = navController
            )
        }

        // BOOKING ENTRY CREATION
        composable("booking/{parkId}") { backStackEntry ->
            val parkId = backStackEntry.arguments
                ?.getString("parkId")
                ?.toIntOrNull() ?: 1
            BookingScreen(
                navController = navController,
                parkId = parkId,
                state = currentContactState.value,
                onEvent = contactViewModel::onEvent
            )
        }

        // RESET ACCOUNT CREDENTIALS SCREEN
        composable("reset_password") {
            ResetPasswordScreen(navController = navController)
        }

        // EXTRA APP UTILITY SCREENS
        composable("map_tracking") { MapTrackingScreen(navController) }

        // ALL NOTIFICATIONS FEED
        composable("notifications") {
            NotificationScreen(
                navController=navController,
                viewModel = bookingViewModel,
                authToken = tokenState.value //It is a variable that stores the user's authentication token.
            )
        }

        // BOOKING NOTIFICATIONS LIST SCREEN
        composable("booking_notifications_list") {
            BookingNotificationsListScreen(
                navController = navController,
                viewModel = bookingViewModel,
                authToken = tokenState.value
            )
        }

        // BROADCAST NOTIFICATIONS LIST SCREEN
        composable("broadcast_notifications_list") {
            BroadcastNotificationsListScreen(
                navController = navController,
                viewModel = bookingViewModel,
                authToken = tokenState.value
            )
        }

        // NOTIFICATION DETAILS
        composable("notification_detail/{notificationId}") { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
            NotificationDetailScreen(
                navController = navController,
                notificationId = notificationId,
                viewModel = bookingViewModel,
                authToken = tokenState.value
            )
        }

        composable("ranger_profile") {
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
            RangerProfileScreen(
                viewModel = rangerViewModel,
                onLogoutSuccess = {
                    onTokenUpdated("")
                    // Redirect to login screen and clear backstack
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                },
                onResetPasswordClick = {
                    navController.navigate("reset_password")
                }
            )


        }

        // BOOKING DETAIL SCREEN (Shows Booking details & Entry QR Code)
        composable("booking_detail/{bookingId}/{bookingReference}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId")?.toIntOrNull() ?: 0
            val bookingReference = backStackEntry.arguments?.getString("bookingReference") ?: ""
            BookingDetailScreen(
                navController = navController,
                bookingId = bookingId,
                bookingReference = bookingReference,
                viewModel = bookingViewModel,
                authToken = tokenState.value
            )
        }
    }
}