package com.example.vistaraapp.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTrackingScreen(navController: NavController) {
    val context = LocalContext.current

    // Brand colors
    val brandGreen = Color(0xFF029602)
    val pureWhite = MaterialTheme.colorScheme.surface
    val darkText = MaterialTheme.colorScheme.onSurface

    // Permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
            isLocationLoading = true
            getCurrentLocation(context) { geoPoint ->
                currentLocation = geoPoint
                isLocationLoading = false
                if (geoPoint == null) {
                    Toast.makeText(context, "Unable to get location. Please enable GPS.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            showPermissionRationale = true
        }
    }

    // Get location if permission already granted when screen first loads
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && currentLocation == null) {
            isLocationLoading = true
            getCurrentLocation(context) { geoPoint ->
                currentLocation = geoPoint
                isLocationLoading = false
            }
        }
    }

    // Initialize MapView and OSMDroid configuration once
    val mapView = remember {
        // Initialize Configuration before inflating MapView
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
        }
    }

    // Add marker representing user location
    val userMarker = remember(mapView) {
        Marker(mapView).apply {
            title = "You are here"
            snippet = "Your current location"
            mapView.overlays.add(this)
        }
    }

    // Bind MapView lifecycle to the lifecycle of the composable
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Tracking",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brandGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhite)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                // Case 1: Permission granted and location available - Show Map
                hasLocationPermission && currentLocation != null -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { mapView },
                        update = { view ->
                            currentLocation?.let { loc ->
                                if (userMarker.position != loc) {
                                    userMarker.position = loc
                                    view.controller.animateTo(loc)
                                    view.invalidate()
                                }
                            }
                        }
                    )
                }
                // Case 2: Permission granted but loading location
                hasLocationPermission && isLocationLoading -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = brandGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Getting your location...",
                            color = darkText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Make sure GPS is enabled",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                // Case 3: Permission not granted - Show request button
                !hasLocationPermission -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = pureWhite),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Location Permission Required",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandGreen
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Vistara needs access to your location to track your position in the park for safety purposes.",
                                    fontSize = 14.sp,
                                    color = darkText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandGreen)
                                ) {
                                    Text(
                                        text = "GRANT PERMISSION",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (showPermissionRationale) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Please enable location permission in Settings to use this feature.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating STOP button
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            ) {
                Text(
                    text = "STOP",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getCurrentLocation(
    context: Context,
    onResult: (GeoPoint?) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult(GeoPoint(location.latitude, location.longitude))
            } else {
                onResult(null)
            }
        }.addOnFailureListener {
            onResult(null)
        }
    } catch (e: SecurityException) {
        onResult(null)
    }
}