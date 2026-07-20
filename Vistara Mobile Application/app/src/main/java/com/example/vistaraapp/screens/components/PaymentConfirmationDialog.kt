package com.example.vistaraapp.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.database.ContactState

@Composable
fun PaymentConfirmationDialog(
    brandGreen: Color,
    state: ContactState,
    parkName: String,
    currentAmount: Double,
    stkPhoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isButtonClicked: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!state.isBookingLoading) onDismiss() },
        title = { Text("Confirm Payment", color = brandGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Park: $parkName", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Total amount: KES $currentAmount", color = brandGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Check-in: ${state.checkInDate}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Checkout: ${state.checkOutDate}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Vehicle Reg: ${state.vehicleRegistration.ifEmpty { "None Specified" }}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Enter Phone Number for ${state.paymentMethod}:",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = stkPhoneNumber,
                    onValueChange = onPhoneNumberChange,
                    placeholder = { Text("e.g. 254712345678") },
                    leadingIcon = { Icon(Icons.Filled.Phone, null, tint = brandGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandGreen,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "An STK prompt will be sent to this phone number to complete the transaction.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isBookingLoading && stkPhoneNumber.isNotBlank() && !isButtonClicked,
                colors = ButtonDefaults.buttonColors(brandGreen)
            ) {
                if (state.isBookingLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Pay Now", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!state.isBookingLoading) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = brandGreen) }
            }
        }
    )
}
