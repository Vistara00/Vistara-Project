package com.example.vistaraapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vistaraapp.R
import com.example.vistaraapp.entities_dataclass.getWeatherDescription
import com.example.vistaraapp.entities_dataclass.getWeatherEmoji
import com.example.vistaraapp.viewmodels.WeatherState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

// 1. THREE-LINE DYNAMIC HERO CARD
@Composable
fun HeroDashboardCard(weatherState: WeatherState) {
    val greeting = getDynamicGreeting()
    val boldPhrase = getDynamicBoldPhrase()

    val heroImages = listOf(
        R.drawable.zebu,
        R.drawable.road,
        R.drawable.one,
        R.drawable.race,
        R.drawable.captivity
    )

    var currentImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4.seconds)
            currentImageIndex = (currentImageIndex + 1) % heroImages.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = heroImages[currentImageIndex]),
            contentDescription = "Nairobi National Park Carousel",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = greeting,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = boldPhrase,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Start your day with nature. 50+ wild animals are waiting for you.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Open Today", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("6:00 AM - 6:00 PM", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (weatherState is WeatherState.Success) {
                        val weather = weatherState.weather
                        val currentTemp = weather.current_weather.temperature.toInt()
                        val weatherCode = weather.current_weather.weathercode

                        Text(text = getWeatherEmoji(weatherCode), fontSize = 28.sp)
                        Text(
                            text = "$currentTemp°C • ${getWeatherDescription(weatherCode)}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        Text("🌤️", fontSize = 28.sp)
                        Text("Loading...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
