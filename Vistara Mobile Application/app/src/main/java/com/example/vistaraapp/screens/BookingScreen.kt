package com.example.vistaraapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.database.ContactEvent
import com.example.vistaraapp.database.ContactState
import com.example.vistaraapp.entities_dataclass.allParks
import com.example.vistaraapp.screens.components.BookingFormContent
import com.example.vistaraapp.screens.components.BookingResultDialog
import com.example.vistaraapp.screens.components.DateRangePickerDialog
import com.example.vistaraapp.screens.components.PaymentConfirmationDialog
import java.time.LocalDate

@Composable
fun PricingBanner(brandGreen: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = brandGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, brandGreen.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = brandGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Park Entry Rates",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = brandGreen
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kenyan Citizen", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("70 KES", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Non-Kenyan Citizen", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100 KES", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    parkId: Int,
    state: ContactState,
    onEvent: (ContactEvent) -> Unit
) {
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val lightGray = MaterialTheme.colorScheme.background
    val darkGray = MaterialTheme.colorScheme.onSurfaceVariant

    val park = allParks.find { it.id == parkId } ?: allParks[0]

    var showDateRangePicker by remember { mutableStateOf(false) }
    var localSelection by remember { mutableStateOf<Pair<LocalDate?, LocalDate?>>(Pair(null, null)) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isButtonClicked by remember { mutableStateOf(false) }
    var stkPhoneNumber by remember { mutableStateOf("") }

    val currentAmount = state.amount

    LaunchedEffect(parkId) {
        onEvent(ContactEvent.ResetBookingState)
    }

    LaunchedEffect(state.showPaymentDialog) {
        if (state.showPaymentDialog) {
            stkPhoneNumber = state.phoneNumber
            isButtonClicked = false
        }
    }

    LaunchedEffect(state.isBookingSuccessful) {
        if (state.isBookingSuccessful) {
            onEvent(ContactEvent.DismissPaymentDialog)
        }
    }

    // Date range picker dialog
    if (showDateRangePicker) {
        DateRangePickerDialog(
            brandGreen = brandGreen,
            initialSelection = localSelection,
            onConfirm = { start, end ->
                onEvent(ContactEvent.EnteredCheckInDate(start.toString()))
                onEvent(ContactEvent.EnteredCheckOutDate(end.toString()))
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }

    // Payment confirmation dialog
    if (state.showPaymentDialog) {
        PaymentConfirmationDialog(
            brandGreen = brandGreen,
            state = state,
            parkName = park.name,
            currentAmount = currentAmount,
            stkPhoneNumber = stkPhoneNumber,
            onPhoneNumberChange = { stkPhoneNumber = it },
            isButtonClicked = isButtonClicked,
            onConfirm = {
                if (!isButtonClicked) {
                    isButtonClicked = true
                    onEvent(ContactEvent.ConfirmBookingPayment(stkPhoneNumber))
                }
            },
            onDismiss = { onEvent(ContactEvent.DismissPaymentDialog) }
        )
    }

    // Payment/Booking Result Dialog
    if (state.isBookingSuccessful || state.isBookingFailed) {
        BookingResultDialog(
            brandGreen = brandGreen,
            state = state,
            parkName = park.name,
            onGoToHome = {
                onEvent(ContactEvent.ResetBookingState)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            onTryAgain = {
                onEvent(ContactEvent.ResetBookingState)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Your Visit", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = brandGreen) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = brandGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhite)
            )
        },
        containerColor = lightGray
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BookingFormContent(
                brandGreen = brandGreen,
                pureWhite = pureWhite,
                park = park,
                state = state,
                currentAmount = currentAmount,
                validationError = validationError,
                onEvent = onEvent,
                onDateFieldClick = {
                    val checkIn = try { LocalDate.parse(state.checkInDate) } catch (_: Exception) { null }
                    val checkOut = try { LocalDate.parse(state.checkOutDate) } catch (_: Exception) { null }
                    localSelection = Pair(checkIn, checkOut)
                    showDateRangePicker = true
                },
                onValidationErrorChange = { validationError = it }
            )
        }
    }
}
