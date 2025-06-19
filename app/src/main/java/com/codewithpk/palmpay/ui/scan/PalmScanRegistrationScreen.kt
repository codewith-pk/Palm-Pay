package com.codewithpk.palmpay.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun PalmScanRegistrationScreen(
    onRegistrationComplete: () -> Unit,
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
            Toast.makeText(context, "Camera permission denied. Cannot register palm.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewWithOverlay(
                scanViewModel = scanViewModel,
                isRegistrationMode = true,
                onScanActionCompleted = onRegistrationComplete
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required to register your palm.", color = MaterialTheme.colorScheme.onBackground)
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
fun CameraPreviewWithOverlay(
    modifier: Modifier = Modifier,
    scanViewModel: ScanViewModel,
    isRegistrationMode: Boolean, // True for enrollment, false for payment scan
    onScanActionCompleted: () -> Unit // Callback when the scan/enroll action is done
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

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

                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                // For registration, we might not need continuous analysis.
                                // For payment, this is where the real-time detection/matching will go.
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
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

        val overlayText = if (isRegistrationMode) "Align your palm to the outline to register." else "Align your palm to verify payment."
        val buttonText = if (isRegistrationMode) "Register Palm" else "Simulate Payment Scan" // Will change for real-time later

        Text(
            text = overlayText,
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-150).dp)
                .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Button(
            onClick = {
                if (isRegistrationMode) {
                    val photoFile = File(
                        context.externalMediaDirs.firstOrNull(),
                        SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                                Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_LONG).show()
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                                val msg = if (isRegistrationMode) "Palm enrolled! Path: $savedUri" else "Payment palm scanned! Path: $savedUri"
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                Log.d(TAG, msg)

                                // Simulate feature extraction and saving to DB
                                scanViewModel.enrollPalm(
                                    userId = "user_001", // Hardcoded for demo; in real app, get from auth
                                    palmImagePath = savedUri.toString(),
                                    mockFeatureData = "mock_palm_feature_${System.currentTimeMillis()}"
                                )
                                onScanActionCompleted() // Navigate away after successful enrollment/simulated payment
                            }
                        }
                    )
                } else {
                    Toast.makeText(context, "Real-time palm scan for payment initiated...", Toast.LENGTH_SHORT).show()
                    // In payment mode, this button might not be needed if auto-scan.
                    // For now, it will act as a trigger, and the image analysis loop will "match".
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(buttonText)
        }
    }
}

@Composable
fun HandOutlineOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Define a simplified hand outline path
        val path = Path().apply {
            moveTo(width * 0.35f, height * 0.5f) // Wrist left
            cubicTo(width * 0.2f, height * 0.4f, width * 0.2f, height * 0.2f, width * 0.3f, height * 0.1f) // Thumb base
            cubicTo(width * 0.35f, height * 0.05f, width * 0.45f, height * 0.05f, width * 0.5f, height * 0.1f) // Thumb tip
            cubicTo(width * 0.6f, height * 0.05f, width * 0.7f, height * 0.05f, width * 0.75f, height * 0.15f) // Index finger tip
            cubicTo(width * 0.85f, height * 0.25f, width * 0.85f, height * 0.45f, width * 0.7f, height * 0.5f) // Pinky finger to wrist
            lineTo(width * 0.35f, height * 0.5f) // Back to start
            close()
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.8f),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
fun AnimatedScanningLine(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanningLineTransition")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -0.3f, // Start above the scan area
        targetValue = 0.3f, // End below the scan area
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // 2 seconds to traverse
            repeatMode = RepeatMode.Reverse
        ), label = "scanningLineOffset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val yPos = size.height * (0.5f + offsetY) // Animate vertically around center
        drawLine(
            color = Color.Green, // Green for scanning
            start = Offset(size.width * 0.2f, yPos),
            end = Offset(size.width * 0.8f, yPos),
            strokeWidth = 4.dp.toPx()
        )
    }
}

private const val TAG = "PalmScanScreen"
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
