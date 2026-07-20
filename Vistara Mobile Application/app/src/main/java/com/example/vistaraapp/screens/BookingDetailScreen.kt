package com.example.vistaraapp.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vistaraapp.viewmodels.BookingUiState
import com.example.vistaraapp.viewmodels.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    navController: NavController,
    bookingId: Int,
    bookingReference: String,
    viewModel: BookingViewModel,
    authToken: String
) {
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface

    val uiState by viewModel.uiState
    // qrCodeState holds the raw base64 string from the server
    val qrBase64 by viewModel.qrCodeState.collectAsState()

    // Decode base64 → Bitmap whenever the value changes
    val qrBitmap = remember(qrBase64) {
        qrBase64?.let { b64 ->
            try {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) { null }
        }
    }

    // Fetch QR code using the NUMERIC booking id
    LaunchedEffect(bookingId, authToken) {
        if (authToken.isNotEmpty()) {
            viewModel.loadQrCode(authToken, bookingId.toString())
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Booking Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brandGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhite)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BookingUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = brandGreen)
                }
            }
            is BookingUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is BookingUiState.Success -> {
                val booking = state.bookings.find { it.bookingReference == bookingReference }

                if (booking == null) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Booking Not Found")
                    }
                } else {
                    val status = booking.bookingStatus ?: "PENDING"
                    val statusColor = when (status.uppercase()) {
                        "CONFIRMED", "COMPLETED" -> Color(0xFF4CAF50)
                        "PENDING" -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status: $status", fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }

                        // Booking Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Booking Information", fontWeight = FontWeight.Bold, color = brandGreen)
                                DetailRow("Reference", booking.bookingReference)
                                DetailRow("Check In", booking.checkInDate)
                                DetailRow("Check Out", booking.checkOutDate)
                                DetailRow("Group Size", "${booking.groupSize ?: 0} person(s)")
                                DetailRow("Vehicle Reg", booking.vehicleRegistration)
                                DetailRow("Amount", "KES ${booking.amount ?: 0.0}")
                                DetailRow("Payment", booking.paymentStatus)
                            }
                        }

                        // QR Code Card — shown when CONFIRMED and QR loaded successfully
                        if (status.uppercase() == "CONFIRMED") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Entry QR Code", fontWeight = FontWeight.Bold, color = brandGreen)
                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap,
                                            contentDescription = "Entry QR Code",
                                            modifier = Modifier.size(220.dp)
                                        )
                                        Text(
                                            text = "Present this QR at the park gate",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            color = brandGreen,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Loading your entry QR code…",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandGreen)
                        ) {
                            Text("Back to Bookings", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}