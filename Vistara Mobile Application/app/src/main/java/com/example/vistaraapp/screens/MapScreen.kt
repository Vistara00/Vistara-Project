package com.example.vistaraapp.screens

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vistaraapp.LocationRepository
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val currentLocation by LocationRepository.currentLocation.collectAsStateWithLifecycle()

    // Tracks whether the map should auto-center on the marker
    var autoFollow by remember { mutableStateOf(true) }

    // Initialize MapView
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)

            // Stop following if user interacts with the map
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    autoFollow = false
                    return true
                }
                override fun onZoom(event: ZoomEvent?): Boolean {
                    autoFollow = false
                    return true
                }
            })
        }
    }

    // Initialize Marker
    val marker = remember(mapView) {
        Marker(mapView).apply {
            title = "Current SOS Location"
            mapView.overlays.add(this)
        }
    }

    // Lifecycle Management
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Render Map
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView },
        update = { view ->
            currentLocation?.let { loc ->
                val geoPoint = GeoPoint(loc.latitude, loc.longitude)

                // 1. Update marker position
                marker.position = geoPoint
                marker.isEnabled = true

                // 2. Animate camera if autoFollow is active
                if (autoFollow) {
                    view.controller.animateTo(geoPoint)
                }

                // 3. Force the MapView (the 'view') to redraw
                view.invalidate()
            }
        }
    )
}