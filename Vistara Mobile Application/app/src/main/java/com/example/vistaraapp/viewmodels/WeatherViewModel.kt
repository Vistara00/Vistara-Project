package com.example.vistaraapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistaraapp.api.WeatherApiClient
import com.example.vistaraapp.entities_dataclass.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    // Nairobi National Park coordinates
    private val latitude = -1.3742
    private val longitude = 36.7833

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    init {
        fetchWeather()
    }

    fun fetchWeather() {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            try {
                val response = WeatherApiClient.weatherService.getWeather(
                    latitude = latitude,
                    longitude = longitude
                )
                _weatherState.value = WeatherState.Success(response)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val weather: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}