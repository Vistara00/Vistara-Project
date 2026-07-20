package com.example.vistaraapp.entities_dataclass

// Weather API Response
data class WeatherResponse(
    val current_weather: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double,
    val weathercode: Int,
    val time: String
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val precipitation: List<Double>
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

// Weather code mapping
fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }
}

fun getWeatherEmoji(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌧️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌧️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}