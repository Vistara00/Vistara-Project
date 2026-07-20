package com.example.vistaraapp.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.database.ContactState

@Composable
fun BookingResultDialog(
    brandGreen: Color,
    state: ContactState,
    parkName: String,
    onGoToHome: () -> Unit,
    onTryAgain: () -> Unit
) {
    val errorColor = Color(0xFFD32F2F)

    AlertDialog(
        onDismissRequest = { /* Modal, must choose an action */ },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null, // Custom title inside content
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isBookingSuccessful) {
                    // Success Tick Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(brandGreen.copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, brandGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = brandGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Payment Request Sent!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandGreen
                    )

                    Text(
                        text = "An M-Pesa STK push prompt has been sent to your phone. Please enter your PIN on your mobile device to complete the payment and confirm your booking.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Booking Details Summary
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BookingSummaryRow("Park:", parkName)
                        BookingSummaryRow("Reference:", state.bookingReference ?: "N/A")
                        BookingSummaryRow("Dates:", "${state.checkInDate} to ${state.checkOutDate}")
                        BookingSummaryRow("Group Size:", "${state.groupSize} Person(s)")
                        BookingSummaryRow("Amount to Pay:", "KES ${state.amount}")
                    }
                } else if (state.isBookingFailed) {
                    // Failure X Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(errorColor.copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, errorColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Failure",
                            tint = errorColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Payment Failed",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = errorColor
                    )

                    Text(
                        text = state.bookingErrorMessage ?: "An unexpected error occurred during payment processing.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isBookingSuccessful) {
                    Button(
                        onClick = onGoToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Return to Home", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (state.isBookingFailed) {
                    Button(
                        onClick = onTryAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = brandGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onGoToHome,
                        border = BorderStroke(1.dp, brandGreen),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = brandGreen),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Return to Home", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun BookingSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}
