package com.example.vistaraapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.vistaraapp.api.RetrofitClient
import com.example.vistaraapp.api_requests_responses.TrackingUpdateRequest
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class EmergencyTrackingService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var token: String? = null
    private var sessionId: Long = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                LocationRepository.updateLocation(location.latitude, location.longitude)
                sendLocationToBackend(location)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == "ACTION_STOP_SOS") {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        token = intent?.getStringExtra("AUTH_TOKEN")
        sessionId = intent?.getLongExtra("SESSION_ID", 0L) ?: 0L

        startForeground(101, createNotification())
        startLocationUpdates()

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 60000 // Update every 1 minute
        ).setMinUpdateIntervalMillis(30000).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun sendLocationToBackend(location: Location) {
        val currentToken = token ?: return
        lifecycleScope.launch {
            try {
                val request = TrackingUpdateRequest(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy.toDouble(),
                    batteryLevel = getBatteryLevel(),
                    sessionId = sessionId
                )

                val response = RetrofitClient.bookingInstance.updateTracking("Bearer $currentToken", request)
                if (response.isSuccessful) {
                    Log.d("TrackingService", "Location sent: ${location.latitude}, ${location.longitude}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "no body"
                    Log.e("TrackingService", "Update failed [${response.code()}]: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("TrackingService", "Update failed: ${e.message}")
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(android.content.Context.BATTERY_SERVICE) as? android.os.BatteryManager
        return batteryManager?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, EmergencyTrackingService::class.java).apply {
            action = "ACTION_STOP_SOS"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "SOS_CHANNEL")
            .setContentTitle("Emergency Active")
            .setContentText("Sharing location with Park Rangers...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Tracking", stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}