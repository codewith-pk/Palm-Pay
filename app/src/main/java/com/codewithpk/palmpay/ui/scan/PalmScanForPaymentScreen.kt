package com.codewithpk.palmpay.ui.scan

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codewithpk.palmpay.data.local.PalmScan // For saving payment transaction
import com.codewithpk.palmpay.ui.theme.GreenSuccess
import com.codewithpk.palmpay.ui.theme.RedFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun PalmScanForPaymentScreen(
    onPaymentProcessed: (isSuccess: Boolean, amount: String, merchant: String) -> Unit,
    onCancel: () -> Unit,
    scanViewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission denied. Cannot verify payment.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPaymentScanContent(
                scanViewModel = scanViewModel,
                onPaymentProcessed = onPaymentProcessed
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required to verify payment.", color = MaterialTheme.colorScheme.onBackground)
                Button(onClick = { cameraLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Permission")
                }
            }
        }

        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
        }
    }
}

@Composable
fun CameraPaymentScanContent(
    modifier: Modifier = Modifier,
    scanViewModel: ScanViewModel,
    onPaymentProcessed: (isSuccess: Boolean, amount: String, merchant: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val enrolledPalm by scanViewModel.enrolledPalm.collectAsState(initial = null)

    var showScanningText by remember { mutableStateOf(false) }
    var processingScan by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                // Simulate real-time hand detection and matching here
                                if (!processingScan) {
                                    // Simulate hand detection (e.g., if any pixels are non-black in a certain area)
                                    val isHandDetected = true // Always assume hand is detected for demo
                                    showScanningText = isHandDetected

                                    if (isHandDetected && enrolledPalm != null) {
                                        processingScan = true // Prevent multiple triggers
                                        // Simulate processing time
                                        CoroutineScope(Dispatchers.Default).launch {
                                            delay(500) // Simulate fast processing, ~0.5 seconds
                                            if (isActive) { // Check if coroutine is still active (not cancelled)
                                                // SIMULATED MATCHING: If a hand is detected and there's an enrolled palm, it's a match!
                                                val isMatch = true
                                                val mockAmount = "1250" // You can pass this from the previous dialog
                                                val mockMerchant = "Kiran Kirana Store"

                                                if (isMatch) {
                                                    // Simulate successful payment
                                                    scanViewModel.savePalmScan(
                                                        userId = enrolledPalm?.userId ?: "unknown_user",
                                                        imageUrl = "n/a", // Not saving image for payment scan
                                                        metadata = "payment_scan_match"
                                                    )
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Palm Matched! Payment Successful!", Toast.LENGTH_SHORT).show()
                                                        onPaymentProcessed(true, mockAmount, mockMerchant)
                                                    }
                                                } else {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Palm Mismatched. Payment Failed.", Toast.LENGTH_SHORT).show()
                                                        onPaymentProcessed(false, mockAmount, mockMerchant)
                                                    }
                                                }
                                                processingScan = false // Reset for next scan attempt if not navigating
                                            }
                                        }
                                    } else if (isHandDetected && enrolledPalm == null) {
                                        // If hand detected but no palm enrolled, prompt enrollment
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(context, "Palm Matched! Payment Successful!", Toast.LENGTH_SHORT).show()
                                            //onPaymentProcessed(true, mockAmount, mockMerchant)
                                        }

                                    }
                                }
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // Using front camera for palm scan in future we will add change to camera as well

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        Log.e(TAG, "Use case binding failed", exc)
                        Toast.makeText(context, "Failed to start camera: ${exc.message}", Toast.LENGTH_LONG).show()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Hand outline overlay
        HandOutlineOverlay()

        // Animated scanning line
        AnimatedScanningLine()

        Text(
            text = "Align your palm to verify payment.",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-150).dp) // Position above the hand outline
                .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (showScanningText) {
            Text(
                text = "Scanning...",
                color = MaterialTheme.colorScheme.primaryContainer,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}