package com.example.vistaraapp.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.database.ContactEvent
import com.example.vistaraapp.database.ContactState
import com.example.vistaraapp.entities_dataclass.NationalPark
import com.example.vistaraapp.screens.PricingBanner

@Composable
fun BookingFormContent(
    brandGreen: Color,
    pureWhite: Color,
    park: NationalPark,
    state: ContactState,
    currentAmount: Double,
    validationError: String?,
    onEvent: (ContactEvent) -> Unit,
    onDateFieldClick: () -> Unit,
    onValidationErrorChange: (String?) -> Unit
) {
    Text(park.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = brandGreen)
    Text(park.location, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

    // About this park card
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(pureWhite)) {
        Column(Modifier.padding(16.dp)) {
            Text("About this park", fontWeight = FontWeight.Bold, color = brandGreen)
            Spacer(modifier = Modifier.height(4.dp))
            Text(park.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            PricingBanner(brandGreen = brandGreen)
        }
    }
    Spacer(Modifier.height(24.dp))

    // Booking details card
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(pureWhite)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Booking Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = brandGreen)

            DatePickerField(label = "Check-in Date", value = state.checkInDate, brandGreen = brandGreen, onClick = onDateFieldClick)
            DatePickerField(label = "Check-out Date", value = state.checkOutDate, brandGreen = brandGreen, onClick = onDateFieldClick)

            OutlinedTextField(
                value = if (state.groupSize == 0) "" else state.groupSize.toString(),
                onValueChange = {
                    val parsedInt = it.toIntOrNull() ?: 0
                    onEvent(ContactEvent.EnteredGroupSize(parsedInt))
                    //AMOUNT
                    onEvent(ContactEvent.EnteredAmount(parsedInt * 100.0))
                },
                label = { Text("Number of People") },
                leadingIcon = { Icon(Icons.Filled.Person, null, tint = brandGreen) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandGreen,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.vehicleRegistration,
                onValueChange = { onEvent(ContactEvent.EnteredVehicleRegistration(it)) },
                label = { Text("Vehicle Registration") },
                placeholder = { Text("e.g. KBC 123A") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandGreen,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Total amount: KES  $currentAmount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = brandGreen)

            Text("Select Payment Method", fontWeight = FontWeight.Medium, color = brandGreen)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = state.paymentMethod == "eCitizen",
                    onClick = { onEvent(ContactEvent.EnteredPaymentMethod("eCitizen")) },
                    label = { Text("eCitizen") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.DarkGray, selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = state.paymentMethod == "MPESA",
                    onClick = { onEvent(ContactEvent.EnteredPaymentMethod("MPESA")) },
                    label = { Text("M-Pesa") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = brandGreen, selectedLabelColor = Color.White
                    )
                )
            }

            val finalErrorMessage = validationError ?: state.bookingErrorMessage
            if (finalErrorMessage != null) {
                Text(finalErrorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    when {
                        state.checkInDate.isBlank() || state.checkOutDate.isBlank() ->
                            onValidationErrorChange("Please complete both date fields")
                        state.groupSize < 1 ->
                            onValidationErrorChange("Enter valid number of people")
                        state.vehicleRegistration.isBlank() ->
                            onValidationErrorChange("Vehicle registration is required")
                        state.paymentMethod.isBlank() ->
                            onValidationErrorChange("Please choose a payment method")
                        else -> {
                            onValidationErrorChange(null)
                            onEvent(ContactEvent.CreateBooking)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(brandGreen)
            ) {
                Text("PROCEED TO PAYMENT", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    brandGreen: Color,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text("YYYY-MM-DD") },
            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = brandGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = brandGreen,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = brandGreen,
                disabledLeadingIconColor = brandGreen
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}
