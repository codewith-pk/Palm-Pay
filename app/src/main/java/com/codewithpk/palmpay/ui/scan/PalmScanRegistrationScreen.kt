package com.codewithpk.palmpay.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.codewithpk.palmpay.data.ml.HandDetectionAnalyzer
import com.codewithpk.palmpay.data.ml.HandDetectionListener
import com.codewithpk.palmpay.data.ml.DetectedHand
import com.google.mediapipe.tasks.vision.core.RunningMode
import android.graphics.PointF // Import Android PointF for canvas drawing
import android.view.ViewTreeObserver // Import for layout listener
import android.view.Surface // Import for Surface rotation constants (e.g., Surface.ROTATION_0)
import androidx.compose.foundation.layout.Arrangement

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

    // State to hold the current dimensions of the PreviewView
    var previewWidth by remember { mutableStateOf(0) }
    var previewHeight by remember { mutableStateOf(0) }
    // State to hold the current display rotation
    var displayRotation by remember { mutableStateOf(0) }


    // Initialize MediaPipe analyzer and observe its detectedHand flow
    val analyzer = remember {
        HandDetectionAnalyzer(context, RunningMode.LIVE_STREAM, object : HandDetectionListener {
            override fun onHandDetected(handRectF: RectF?, landmarks: List<PointF>?) {
                // The analyzer directly updates its internal StateFlow `_detectedHand`.
                // We'll observe `analyzer.detectedHand` directly in the composable.
            }
        })
    }
    // Collect the latest detected hand state from the analyzer's StateFlow
    val detectedHand by analyzer.detectedHand.collectAsState()


    DisposableEffect(Unit) {
        // This effect runs once when the composable enters the composition.
        // It's responsible for managing the lifecycle of the analyzer.
        onDispose {
            cameraExecutor.shutdown() // Shut down the executor for ImageAnalysis
            analyzer.stop() // Stop MediaPipe detector gracefully to release its resources
        }
    }

    Box(modifier = modifier) {
        // AndroidView to embed the CameraX PreviewView into Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER // Scale to fill the view, maintaining aspect ratio

                    // Add a listener to get the actual dimensions of the PreviewView once it's laid out
                    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            previewWidth = width
                            previewHeight = height
                            // NEW: Get display rotation here and update state
                            displayRotation = this@apply.display?.rotation ?: 0
                            viewTreeObserver.removeOnGlobalLayoutListener(this) // Remove listener to avoid multiple calls
                        }
                    })
                }
            },
            update = { previewView -> // `previewView` is now accessible here
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    // Setup Preview Use Case
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // Setup ImageCapture Use Case (for taking a photo during enrollment)
                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display.rotation) // Use display rotation
                        .build()

                    // Setup ImageAnalysis Use Case (for real-time hand detection with MediaPipe)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, analyzer) // Set our MediaPipe analyzer here
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // Use front camera for palm scan

                    try {
                        // Unbind any previous use cases before rebinding
                        cameraProvider.unbindAll()
                        // Bind all use cases to the lifecycle owner
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture, // Bind ImageCapture for taking photos
                            imageAnalysis // Bind ImageAnalysis for real-time hand detection
                        )
                    } catch (exc: Exception) {
                        Log.e(TAG, "Use case binding failed", exc)
                        Toast.makeText(context, "Failed to start camera: ${exc.message}", Toast.LENGTH_LONG).show()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Draw dynamic Hand Overlay based on MediaPipe detection results
        // Pass the actual dimensions of the PreviewView for correct scaling
        if (previewWidth > 0 && previewHeight > 0) { // Only draw if dimensions are known
            DynamicHandOverlay(
                detectedHand?.boundingBox,
                detectedHand?.landmarks,
                previewViewWidth = previewWidth.toFloat(),
                previewViewHeight = previewHeight.toFloat(),
                imageWidth = detectedHand?.inputImageWidth?.toFloat() ?: 0f, // Pass original image width
                imageHeight = detectedHand?.inputImageHeight?.toFloat() ?: 0f, // Pass original image height
                isFrontCamera = true, // Assuming front camera, adjust if camera is dynamic
                displayRotation = displayRotation // Pass the state variable for display rotation
            )
        }
        // Animated scanning line (visual effect)
        AnimatedScanningLine()

        val isHandVisible = detectedHand?.boundingBox != null
        // Dynamic overlay text based on hand visibility and mode
        val overlayText = when {
            isRegistrationMode && !isHandVisible -> "Align your hand to the outline."
            isRegistrationMode && isHandVisible -> "Hold still. Analyzing your palm lines..."
            !isRegistrationMode && !isHandVisible -> "Align your palm to verify payment."
            !isRegistrationMode && isHandVisible -> "Verifying payment with palm scan..."
            else -> "Processing..."
        }

        Text(
            text = overlayText,
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-150).dp) // Position text above the hand area
                .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium) // Semi-transparent background
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Button(
            onClick = {
                if (isRegistrationMode) {
                    // Only allow photo capture if a hand is currently detected by MediaPipe
                    if (detectedHand?.boundingBox != null) {
                        val photoFile = File(
                            context.externalMediaDirs.firstOrNull(), // Use external media directory
                            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        imageCapture?.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context), // Executor for callbacks
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exc: ImageCaptureException) {
                                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                                    Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_LONG).show()
                                }

                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                                    val msg = "Palm enrolled! Path: $savedUri"
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    Log.d(TAG, msg)

                                    // Simulate feature extraction and saving to DB
                                    // In a real app, you would pass the captured image data to an ML model
                                    // here to extract unique biometric features for storage.
                                    scanViewModel.enrollPalm(
                                        userId = "user_001", // Hardcoded user ID for demo; in real app, get from auth
                                        palmImagePath = savedUri.toString(),
                                        // Use detected landmarks as a mock 'pattern' feature data
                                        mockFeatureData = detectedHand?.landmarks?.joinToString(",") { "${it.x},${it.y}" } ?: "no_landmarks_captured"
                                    )
                                    // Log a scan for "Scans Made" counter (enrollment is also a form of scan)
                                    scanViewModel.savePalmScan(
                                        userId = "user_001",
                                        imageUrl = savedUri.toString(),
                                        metadata = "Palm Enrollment",
                                        amount = 0.0
                                    )
                                    onScanActionCompleted() // Navigate away after successful enrollment
                                }
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please align your hand in the frame first.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // This button is less critical in real-time mode as detection is continuous.
                    // It could be used to manually trigger a single scan or just indicate ready state.
                    Toast.makeText(context, "Real-time palm scan for payment initiated...", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(if (isRegistrationMode) "Enroll Palm" else "Start Scan")
        }
    }
}

// These are shared Composables for drawing overlays. They can be moved to a common file like ui.components
@Composable
fun DynamicHandOverlay(
    handRectF: RectF?,
    landmarks: List<PointF>?,
    previewViewWidth: Float, // Actual width of the PreviewView
    previewViewHeight: Float, // Actual height of the PreviewView
    imageWidth: Float, // Original width of the image processed by MediaPipe
    imageHeight: Float, // Original height of the image processed by MediaPipe
    isFrontCamera: Boolean, // True if using front camera (for mirroring)
    displayRotation: Int, // NEW: Pass the display rotation
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (imageWidth == 0f || imageHeight == 0f || previewViewWidth == 0f || previewViewHeight == 0f) {
            return@Canvas
        }

        // Calculate scaling factors from original image resolution to preview view resolution
        val scaleX = previewViewWidth / imageWidth
        val scaleY = previewViewHeight / imageHeight

        val effectiveScale: Float
        val xOffset: Float
        val yOffset: Float

        val previewAspectRatio = previewViewWidth / previewViewHeight
        val imageAspectRatio = imageWidth / imageHeight

        if (previewAspectRatio > imageAspectRatio) { // Preview is wider (letterboxing horizontally)
            effectiveScale = previewViewHeight / imageHeight
            xOffset = (previewViewWidth - imageWidth * effectiveScale) / 2f
            yOffset = 0f
        } else { // Preview is taller or same aspect ratio (letterboxing vertically)
            effectiveScale = previewViewWidth / imageWidth
            xOffset = 0f
            yOffset = (previewViewHeight - imageHeight * effectiveScale) / 2f
        }

        // Function to transform a single point from original image coordinates to canvas coordinates
        fun transformPoint(point: PointF): Offset {
            var transformedX = point.x
            var transformedY = point.y

            // Apply rotation and mirroring logic based on device display rotation and camera type
            // This logic assumes MediaPipe gives results relative to the raw image (usually landscape for sensor)
            // and PreviewView rotates this for display.
            when (displayRotation) {
                Surface.ROTATION_0 -> { // Device in portrait (upright)
                    // Raw image (e.g., 640x480 landscape) is rotated 90deg CW to fit portrait display.
                    // So, swap X and Y, and adjust Y for the new origin.
                    val tempX = transformedX
                    transformedX = transformedY // Old Y becomes new X
                    transformedY = imageWidth - tempX // Old X becomes new Y, flipped horizontally
                }
                Surface.ROTATION_90 -> { // Device in landscape (left)
                    // Image is already landscape. No coordinate swap needed.
                    // Just potential mirroring.
                }
                Surface.ROTATION_180 -> { // Device in upside-down portrait
                    // Raw image rotated 270deg CW.
                    // Needs full 180deg rotation: flip X and Y relative to image dimensions.
                    transformedX = imageWidth - transformedX
                    transformedY = imageHeight - transformedY
                }
                Surface.ROTATION_270 -> { // Device in upside-down landscape (right)
                    // Raw image rotated 90deg CCW.
                    // Swap X and Y, and adjust X for new origin.
                    val tempX = transformedX
                    transformedX = imageHeight - transformedY // Old Y becomes new X, flipped
                    transformedY = tempX // Old X becomes new Y
                }
            }

            // Apply horizontal flip for front camera *after* rotation compensation.
            // This corrects the mirror effect.
            if (isFrontCamera) {
                // The width to flip against depends on whether the image was conceptually rotated.
                val effectiveWidthForFlip = when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> imageHeight // If rotated to portrait, effective width is original height
                    else -> imageWidth // If landscape, effective width is original width
                }
                transformedX = effectiveWidthForFlip - transformedX
            }

            // Scale coordinates to PreviewView dimensions and apply letterboxing offsets
            return Offset(
                x = (transformedX * effectiveScale) + xOffset,
                y = (transformedY * effectiveScale) + yOffset
            )
        }

        // Draw bounding box (optional, can be removed to match image)
        // You can comment out or remove this if you only want the skeletal lines and dots.
//        handRectF?.let { rect ->
//            val topLeft = transformPoint(PointF(rect.left, rect.top))
//            val topRight = transformPoint(PointF(rect.right, rect.top))
//            val bottomLeft = transformPoint(PointF(rect.left, rect.bottom))
//            val bottomRight = transformPoint(PointF(rect.right, rect.bottom))
//
//            drawPath(
//                path = Path().apply {
//                    moveTo(topLeft.x, topLeft.y)
//                    lineTo(topRight.x, topRight.y)
//                    lineTo(bottomRight.x, bottomRight.y)
//                    lineTo(bottomLeft.x, bottomLeft.y)
//                    close()
//                },
//                color = Color.Transparent, // Changed to Transparent to match the example image (no bounding box)
//                style = Stroke(width = 0.dp.toPx()) // No stroke for transparent
//            )
//        }

        // Draw landmarks as green circles
        landmarks?.forEach { point ->
            val transformedPoint = transformPoint(point)
            drawCircle(
                color = Color.Green, // Green dots for landmarks
                center = transformedPoint,
                radius = 8.dp.toPx() // Slightly larger dots for visibility
            )
        }

        // Draw lines connecting landmarks (skeletal structure - Red lines)
        if (!landmarks.isNullOrEmpty()) {
            val connections = listOf(
                // Wrist to base of fingers (Palm base)
                0 to 1, // Wrist to Thumb_CMC
                0 to 5, // Wrist to Index_MCP
                0 to 9, // Wrist to Middle_MCP
                0 to 13, // Wrist to Ring_MCP
                0 to 17, // Wrist to Pinky_MCP

                // Thumb connections
                1 to 2, 2 to 3, 3 to 4,
                // Index finger connections
                5 to 6, 6 to 7, 7 to 8,
                // Middle finger connections
                9 to 10, 10 to 11, 11 to 12,
                // Ring finger connections
                13 to 14, 14 to 15, 15 to 16,
                // Pinky finger connections
                17 to 18, 18 to 19, 19 to 20,

                // Connections between finger bases for the palm outline
                5 to 9,
                9 to 13,
                13 to 17
            )

            connections.forEach { (startIdx, endIdx) ->
                if (landmarks.size > startIdx && landmarks.size > endIdx) {
                    val startPoint = transformPoint(landmarks[startIdx])
                    val endPoint = transformPoint(landmarks[endIdx])
                    drawLine(
                        color = Color.Red, // Red lines for connections
                        start = startPoint,
                        end = endPoint,
                        strokeWidth = 4.dp.toPx() // Thickness of skeletal lines
                    )
                }
            }
        }
        // REMOVED: Simulated palm lines (yellow and cyan lines) to match the reference image.
    }
}


@Composable
fun AnimatedScanningLine(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanningLineTransition")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, // Start at the top edge of the scan area
        targetValue = 1f, // End at the bottom edge of the scan area
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // 2 seconds for one pass
            repeatMode = RepeatMode.Reverse // Scan up and down
        ), label = "scanningLineOffset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val yPos = size.height * offsetY

        drawLine(
            color = Color(0xFF4CAF50), // Green color for the scanning line
            start = Offset(0f, yPos), // Start from left edge of canvas
            end = Offset(size.width, yPos), // End at right edge of canvas
            strokeWidth = 4.dp.toPx() // Thickness of the scanning line
        )
    }
}

private const val TAG = "PalmScanScreen"
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
