package com.example.vistaraapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import com.example.vistaraapp.entities_dataclass.UniqueAnimal

@OptIn(ExperimentalMaterial3Api::class) // Required for using TopAppBar components
@SuppressLint("LocalContextResourcesRead")
@Composable
fun AnimalDetailScreen(
    animal: UniqueAnimal,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    // Coil's AsyncImage is for efficient loading and automatic downsampling
    // No manual bitmap handling is needed; AsyncImage will cache and resize appropriately

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        //NEW TOP APP BAR (BACK BUTTON)
        topBar = {
            TopAppBar(
                title = { Text(text = "Details", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        // his line drops the current screen and takes you back!
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = Color(0xFF029602)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // This padding automatically accounts for the top bar height!
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
    model = animal.imageRes,
    contentDescription = animal.name,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp)
        .clip(RoundedCornerShape(24.dp)),
    contentScale = ContentScale.Crop
)

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = animal.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = animal.scientificName,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = animal.description,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF1B3B2B) else Color(0xFFE8F5E9)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fun Fact",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = animal.funFact,
                            color = Color(0xFF029602)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Best Time to Spot: ${animal.bestTimeToSee}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate("booking/1")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF029602),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Book Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}