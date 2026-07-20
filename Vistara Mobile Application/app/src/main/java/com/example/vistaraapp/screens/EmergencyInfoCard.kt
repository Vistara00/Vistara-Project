package com.example.vistaraapp.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.vistaraapp.ui.theme.VistaraTheme

data class EmergencyType(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyInfoCard(
    onSendEmergencyReport: (type: String, details: String) -> Unit,
    modifier: Modifier = Modifier,
    brandGreen: Color = Color(0xFF029602)
) {
    val context = LocalContext.current
    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf("") }
    var additionalDetails by remember { mutableStateOf("") }

    val emergencyCategories = remember {
        listOf(
            EmergencyType("Wildlife Encounter", Icons.Default.Pets),
            EmergencyType("Medical ", Icons.Default.LocalHospital),
            EmergencyType("Accident", Icons.Default.Whatshot),
            EmergencyType("Lost", Icons.Default.Policy),
            EmergencyType("General Distress", Icons.Default.LocationOn),
            EmergencyType("Vehicle Breakdown", Icons.Default.Build)
        )
    }

    // Moved dialog wrapper out here to guarantee rendering context outside the sheet's lifecycle
    if (showSuccessDialog) {
        SuccessAlertDialog(
            visible = showSuccessDialog,
            brandGreen = brandGreen,
            onDismiss = { showSuccessDialog = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Emergency Contacts",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(8.dp))

            EmergencyContactRow(
                icon = Icons.Default.Shield,
                label = "Park Rangers",
                number = "0700 597 000",
                textColor = brandGreen
            ) {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:0700597000".toUri()))
            }
            EmergencyContactRow(
                icon = Icons.Default.MedicalServices,
                label = "Ambulance",
                number = "999",
                textColor = brandGreen
            ) {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:999".toUri()))
            }
            EmergencyContactRow(
                icon = Icons.Default.Policy,
                label = "Police",
                number = "112",
                textColor = brandGreen
            ) {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:112".toUri()))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isBottomSheetOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(35.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(
                    text = "SOS EMERGENCY REPORT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            SafetyTipsSection()
        }
    }

    if (isBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isBottomSheetOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Report Emergency Incident",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = "Select a category to speed up field-ranger response.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                CategoryGrid(
                    categories = emergencyCategories,
                    selectedType = selectedType,
                    onTypeSelect = { selectedType = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    label = { Text("Describe the situation (optional)") },
                    placeholder = { Text("e.g., Elephant blocking trail, flat tire") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = Color(0xFFD32F2F),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = Color(0xFFD32F2F)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                ActionButtonsRow(
                    isSubmitEnabled = selectedType.isNotEmpty(),
                    onCancel = { isBottomSheetOpen = false },
                    onSubmit = {
                        onSendEmergencyReport(selectedType, additionalDetails)
                        isBottomSheetOpen = false
                        // Triggers state rebuild instantly now that it is anchored explicitly outside the sheet block context
                        showSuccessDialog = true

                        // Clear entry fields clean for subsequent reports
                        selectedType = ""
                        additionalDetails = ""
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emergency & Safety",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFD32F2F).copy(alpha = 0.15f)
        ) {
            Text(
                text = "ACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SafetyTipsSection() {
    Text(
        text = " Safety Tips",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF029602)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "• Stay inside your vehicle at all times",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = "• Keep a safe distance from animals",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun CategoryGrid(
    categories: List<EmergencyType>,
    selectedType: String,
    onTypeSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val chunks = categories.chunked(2)
        chunks.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    val isSelected = selectedType == item.label
                    val isDark = isSystemInDarkTheme()
                    OutlinedCard(
                        onClick = { onTypeSelect(item.label) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) {
                                if (isDark) Color(0xFF3E1E22) else Color(0xFFFFEBEE)
                            } else {
                                Color.Transparent
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = item.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    isSubmitEnabled: Boolean,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onSubmit,
            enabled = isSubmitEnabled,
            modifier = Modifier.weight(2f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                disabledContainerColor = if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFE0E0E0),
                disabledContentColor = if (isSystemInDarkTheme()) Color(0xFF666666) else Color.Gray
            )
        ) {
            Text("SUBMIT ALERT", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessAlertDialog(
    visible: Boolean,
    brandGreen: Color,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = brandGreen)
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = brandGreen,
                    modifier = Modifier.size(64.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Done",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your emergency report has been submitted successfully.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EmergencyContactRow(
    icon: ImageVector,
    label: String,
    number: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
        Text(text = number, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Preview(showBackground = true)
@Composable
fun EmergencyInfoCardPreview() {
    VistaraTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            EmergencyInfoCard(
                onSendEmergencyReport = { _, _ -> }
            )
        }
    }
}