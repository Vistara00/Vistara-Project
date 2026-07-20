package com.example.vistaraapp.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.vistaraapp.QrScannerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun RangerScannerScreen(
    qrScannerViewModel: QrScannerViewModel,
    token: String
) {
    var scannedOnce by remember { mutableStateOf(false) }
    var scannerMode by remember { mutableStateOf("OPTIONS") } // "OPTIONS", "CAMERA"
    val qrScanState by qrScannerViewModel.uiState

    Box(modifier = Modifier.fillMaxSize()) {
        when (scannerMode) {
            "OPTIONS" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Visitor QR Check-In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap below when you are ready to scan the visitor's QR code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { scannerMode = "CAMERA"; scannedOnce = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Scan QR Code (Camera)")
                    }
                }
            }

            "CAMERA" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    RangerScannerContent(onResult = { qrData ->
                        if (!scannedOnce) {
                            scannedOnce = true
                            Log.d("QRScanner", "QR decoded: $qrData")
                            qrScannerViewModel.verifyQrCode(token, qrData)
                        }
                    })
                    Button(
                        onClick = { scannerMode = "OPTIONS"; scannedOnce = false },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }

        // Result Dialogs for API 1 and API 2
        when (val state = qrScanState) {
            is QrScannerViewModel.QrScannerUiState.Loading -> {
                Dialog(onDismissRequest = {}) {
                    CircularProgressIndicator()
                }
            }
            is QrScannerViewModel.QrScannerUiState.Verified -> {
                val details = state.details
                AlertDialog(
                    onDismissRequest = { qrScannerViewModel.reset(); scannedOnce = false },
                    title = { Text("Booking Verified") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ref: ${details.bookingReference}")
                            Text("Check-In Date: ${details.checkInDate}")
                            Text("Check-Out Date: ${details.checkOutDate}")
                            Text("Payment: ${details.paymentStatus}")
                            Text("Booking Status: ${details.bookingStatus}")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { qrScannerViewModel.reset(); scannedOnce = false }) {
                            Text("Cancel")
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            // Trigger API 2: Check-In Visitor
                            qrScannerViewModel.checkInVisitor(token, state.qrData)
                        }) {
                            Text("Check In Visitor")
                        }
                    }
                )
            }
            is QrScannerViewModel.QrScannerUiState.CheckedIn -> {
                AlertDialog(
                    onDismissRequest = {
                        qrScannerViewModel.reset()
                        scannedOnce = false
                        scannerMode = "OPTIONS"
                    },
                    title = { Text("Visitor Checked In") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(state.message)
                            state.visitorName?.let { Text("Visitor: $it") }
                            state.bookingReference?.let { Text("Ref: $it") }
                            state.status?.let { Text("Status: $it") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            qrScannerViewModel.reset()
                            scannedOnce = false
                            scannerMode = "OPTIONS"
                        }) {
                            Text("Done")
                        }
                    }
                )
            }
            is QrScannerViewModel.QrScannerUiState.Error -> {
                AlertDialog(
                    onDismissRequest = { qrScannerViewModel.reset(); scannedOnce = false },
                    title = { Text("Scan / Check-In Failed") },
                    text = { Text(state.message) },
                    confirmButton = {
                        TextButton(onClick = { qrScannerViewModel.reset(); scannedOnce = false }) {
                            Text("Retry")
                        }
                    }
                )
            }
            else -> { /* Idle — do nothing */ }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun RangerScannerContent(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val cameraProvider = cameraProviderFuture.get()

        // 1. Setup Preview
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        // 2. Setup Barcode Scanner for QR specifically
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)

        // 3. Setup Image Analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { scannedData ->
                                        Log.d("QRScanner", "Barcode detected by camera: $scannedData")
                                        onResult(scannedData)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("QRScanner", "Scanning process failed", e)
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        // 4. Bind to Lifecycle once
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
            Log.d("QRScanner", "Camera successfully bound to lifecycle")
        } catch (e: Exception) {
            Log.e("QRScanner", "Camera binding failed", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder Overlay
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        )

        Text(
            text = "Align QR Code",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}