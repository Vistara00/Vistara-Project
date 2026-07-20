package com.example.vistaraapp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun MapButton(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            // Navigate directly to Live Map Tracking Screen
            navController.navigate("map_tracking")
        },
        modifier = modifier,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF029602),
            contentColor = androidx.compose.ui.graphics.Color.White
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Text("View Live Tracking")
    }
}