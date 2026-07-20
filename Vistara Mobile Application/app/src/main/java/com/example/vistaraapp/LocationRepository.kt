package com.example.vistaraapp

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LocationRepository {
    // Explicitly define the type as Location? to allow null values
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    fun updateLocation(lat: Double, lon: Double) {
        val loc = Location("provider").apply {
            latitude = lat
            longitude = lon
        }
        _currentLocation.value = loc
    }
}