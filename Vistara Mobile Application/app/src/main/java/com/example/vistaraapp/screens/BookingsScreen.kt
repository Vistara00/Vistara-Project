package com.example.vistaraapp.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
// FIX: Resolved the deprecation warning by importing from the modern compose runtime library
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.vistaraapp.viewmodels.BookingUiState
import com.example.vistaraapp.viewmodels.BookingViewModel
import com.example.vistaraapp.api_requests_responses.BookingData

@Composable
fun BookingsScreen(
    navController: NavController,
    viewModel: BookingViewModel,
    authToken: String
) {
    val brandGreen = Color(0xFF029602)
    val lightGray = MaterialTheme.colorScheme.background
    val parkName = "Nairobi National Park"
    val context = LocalContext.current

    // Triggers the network API fetch every time the screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchBookings(authToken)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cancellationStatus by viewModel.cancellationStatus

    LaunchedEffect(cancellationStatus) {
        cancellationStatus?.let { statusMessage ->
            Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
            viewModel.clearCancellationStatus()
        }
    }

    val uiState by viewModel.uiState

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = lightGray
    ) {
        when (val state = uiState) {
            is BookingUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = brandGreen)
                }
            }
            is BookingUiState.Error -> {
                ErrorState(message = state.message, onRetry = { viewModel.fetchBookings(authToken) }, brandGreen = brandGreen)
            }
            is BookingUiState.Success -> {
                val networkBookings = state.bookings

                if (networkBookings.isEmpty()) {
                    EmptyBookingsState(navController, brandGreen, parkName)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = parkName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandGreen,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        val upcomingBookings = networkBookings.filter { it.bookingStatus == "CONFIRMED" || it.bookingStatus == "PENDING" }
                        val pastBookings = networkBookings.filter { it.bookingStatus == "COMPLETED" || it.bookingStatus == "CANCELLED" }

                        if (upcomingBookings.isNotEmpty()) {
                            item { SectionHeader(title = "Upcoming Visits", brandGreen = brandGreen) }
                            items(upcomingBookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    parkName = parkName,
                                    onCancel = {
                                        booking.id?.let { id ->
                                            viewModel.cancelBooking(authToken, id.toString())
                                        }
                                    },
                                    onPay = {
                                        booking.bookingReference?.let { ref ->
                                            viewModel.initiateMpesaPayment(authToken, ref)
                                        }
                                    },
                                    onClick = {
                                        val id = booking.id ?: return@BookingCard
                                        val ref = booking.bookingReference ?: return@BookingCard
                                        navController.navigate("booking_detail/$id/$ref")
                                    },
                                    brandGreen = brandGreen
                                )
                            }
                        }

                        if (pastBookings.isNotEmpty()) {
                            item { SectionHeader(title = "Past Visits", brandGreen = brandGreen) }
                            items(pastBookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    parkName = parkName,
                                    onCancel = null,
                                    onPay = null,
                                    onClick = {
                                        val id = booking.id ?: return@BookingCard
                                        val ref = booking.bookingReference ?: return@BookingCard
                                        navController.navigate("booking_detail/$id/$ref")
                                    },
                                    brandGreen = brandGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, brandGreen: Color) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = brandGreen,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun BookingCard(
    booking: BookingData,
    parkName: String,
    onCancel: (() -> Unit)?,
    onPay: (() -> Unit)?,
    onClick: (() -> Unit)? = null,
    brandGreen: Color
) {
    val status = booking.bookingStatus ?: "PENDING"
    val statusColor = when (status.uppercase()) {
        "CONFIRMED", "COMPLETED" -> Color(0xFF4CAF50)
        "PENDING" -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val mpesaAmber = Color(0xFFFFB300)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parkName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow(label = "Check In Date", value = booking.checkInDate)
            DetailRow(label = "Check Out Date", value = booking.checkOutDate)
            DetailRow(label = "Vehicle Reg", value = booking.vehicleRegistration)
            DetailRow(label = "Group Size", value = "${booking.groupSize ?: 0} person(s)")
            DetailRow(label = "Total Amount", value = "KES ${booking.amount ?: 0.0}", valueColor = brandGreen)
            DetailRow(label = "Booking Ref", value = booking.bookingReference)

            if (status.uppercase() == "PENDING" && onPay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPay,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandGreen)
                ) {
                    Text("Make Payment (M-Pesa)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if ((status.uppercase() == "CONFIRMED" || status.uppercase() == "PENDING") && onCancel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                    border = BorderStroke(1.dp, Color(0xFFF44336))
                ) {
                    Text("Cancel Booking", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String?, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(text = value ?: "N/A", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun EmptyBookingsState(navController: NavController, brandGreen: Color, parkName: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Bookings Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = brandGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You haven't booked a safari at $parkName yet.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("wildlife") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandGreen)
        ) {
            Text("Book Now", color = Color.White)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, brandGreen: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Something Went Wrong", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}