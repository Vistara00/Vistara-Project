package com.example.vistaraapp.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.database.ContactState
import com.example.vistaraapp.entities_dataclass.NationalPark

@Composable
fun BookingSuccessContent(
    brandGreen: Color,
    pureWhite: Color,
    darkGray: Color,
    park: NationalPark,
    state: ContactState,
    currentAmount: Double,
    onCheckInNow: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = brandGreen.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Payment Successful!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = brandGreen)
            Text("Your booking has been confirmed.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Spacer(Modifier.height(32.dp))

    Text(
        text = "Booking Details",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = brandGreen,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = pureWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookingDetailRow("Park:", park.name, darkGray)
            BookingDetailRow("Group Size:", "${state.groupSize}", darkGray)
            BookingDetailRow("Vehicle Reg:", state.vehicleRegistration.ifEmpty { "N/A" }, darkGray)
            BookingDetailRow("Check-in date:", state.checkInDate, darkGray)
            BookingDetailRow("Check-out date:", state.checkOutDate, darkGray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Amount:", fontSize = 16.sp, color = brandGreen, fontWeight = FontWeight.Bold)
                Text("KES $currentAmount", fontSize = 16.sp, color = brandGreen, fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(Modifier.height(32.dp))

}

@Composable
private fun BookingDetailRow(label: String, value: String, darkGray: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = darkGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}
