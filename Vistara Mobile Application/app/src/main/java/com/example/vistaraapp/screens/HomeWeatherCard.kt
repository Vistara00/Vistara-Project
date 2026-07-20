package com.example.vistaraapp.screens

import androidx.compose.foundation.clickable
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
import com.example.vistaraapp.entities_dataclass.getWeatherDescription
import com.example.vistaraapp.entities_dataclass.getWeatherEmoji
import com.example.vistaraapp.viewmodels.WeatherState

// 3. REAL-TIME WEATHER CARD
@Composable
fun RealTimeWeatherCard(
    brandGreen: Color,
    weatherState: WeatherState,
    onRetryWeather: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (weatherState) {
                is WeatherState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Loading weather...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                is WeatherState.Success -> {
                    val weather = weatherState.weather
                    val currentTemp = weather.current_weather.temperature.toInt()
                    val weatherCode = weather.current_weather.weathercode
                    val weatherEmoji = getWeatherEmoji(weatherCode)
                    val weatherDesc = getWeatherDescription(weatherCode)

                    Text(text = weatherEmoji, fontSize = 40.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Vistara Weather",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = brandGreen
                        )
                        Text(
                            text = "$currentTemp°C, $weatherDesc",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentTemp in 20..28) "Perfect for a safari!" else "Plan your visit accordingly",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is WeatherState.Error -> {
                    Text("⚠️", fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Weather Unavailable",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap to retry",
                            fontSize = 12.sp,
                            color = brandGreen,
                            modifier = Modifier.clickable { onRetryWeather() }
                        )
                    }
                }
            }
        }
    }
}
