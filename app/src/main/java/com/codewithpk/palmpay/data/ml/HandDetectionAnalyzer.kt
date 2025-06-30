package com.codewithpk.palmpay.data.ml

import android.content.Context
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.graphics.PointF // Android PointF for drawing
import android.graphics.Bitmap // For ImageProxy to Bitmap conversion
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

import java.io.IOException

// Interface to pass results back to Composable
interface HandDetectionListener {
    fun onHandDetected(handRectF: RectF?, landmarks: List<PointF>?)
}

// Data class to hold detected hand information
data class DetectedHand(
    val boundingBox: RectF?,
    val landmarks: List<PointF>?,
    val inputImageWidth: Int,
    val inputImageHeight: Int
)

class HandDetectionAnalyzer(
    private val context: Context,
    private val runningMode: RunningMode,
    private val listener: HandDetectionListener
) : ImageAnalysis.Analyzer {

    private var handLandmarker: HandLandmarker? = null
    private val backgroundExecutor = Executors.newSingleThreadExecutor()

    // Use a MutableStateFlow to emit hand detection results for Composables to observe
    private val _detectedHand = MutableStateFlow<DetectedHand?>(null)
    val detectedHand = _detectedHand.asStateFlow()

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        backgroundExecutor.execute {
            try {
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task") // This is the .task model file
                    .setDelegate(Delegate.GPU) // Try GPU delegate for performance, fallback to CPU
                    .build()

                val optionsBuilder = HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setNumHands(1) // Detect up to 1 hand for payment/enrollment
                    .setMinHandDetectionConfidence(0.5f)
                    .setMinTrackingConfidence(0.5f)
                    .setRunningMode(runningMode)
                    .setResultListener(this::onResults) // Callback for live stream
                    .setErrorListener(this::onError)     // Callback for errors
                    .build()

                handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder)
                Log.d(TAG, "MediaPipe HandLandmarker initialized successfully.")

            } catch (e: IOException) {
                Log.e(TAG, "Failed to load MediaPipe model: ${e.message}. Make sure hand_landmarker.task is in src/main/assets", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MediaPipe HandLandmarker: ${e.message}", e)
            }
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val originalBitmap = imageProxy.toBitmap() // Convert ImageProxy to Bitmap
        val grayscaleBitmap = toGrayscale(originalBitmap) // NEW: Convert to grayscale

        val mpImage = BitmapImageBuilder(grayscaleBitmap).build() // Use grayscale bitmap for MediaPipe
        val frameTimeMs = System.currentTimeMillis()

        when (runningMode) {
            RunningMode.LIVE_STREAM -> {
                if (handLandmarker != null) {
                    handLandmarker?.detectAsync(mpImage, frameTimeMs)
                } else {
                    Log.w(TAG, "HandLandmarker not initialized, skipping frame.")
                }
            }
            else -> {
                Log.e(TAG, "Unsupported running mode: $runningMode")
            }
        }
        imageProxy.close() // IMPORTANT: Always close the imageProxy once done with it!
        originalBitmap.recycle() // Recycle original bitmap to free memory
        grayscaleBitmap.recycle() // Recycle grayscale bitmap
    }

    private fun onResults(result: HandLandmarkerResult, inputImage: MPImage) {
        if (result.landmarks().isNotEmpty()) {
            val firstHandLandmarks = result.landmarks()[0]

            val xCoords = firstHandLandmarks.map { it.x() }.toFloatArray()
            val yCoords = firstHandLandmarks.map { it.y() }.toFloatArray()

            val handRectF = if (xCoords.isNotEmpty() && yCoords.isNotEmpty()) {
                RectF(
                    xCoords.minOrNull() ?: 0f,
                    yCoords.minOrNull() ?: 0f,
                    xCoords.maxOrNull() ?: inputImage.width.toFloat(),
                    yCoords.maxOrNull() ?: inputImage.height.toFloat()
                )
            } else null

            val pixelLandmarks = firstHandLandmarks.map { landmark ->
                PointF(
                    landmark.x() * inputImage.width,
                    landmark.y() * inputImage.height
                )
            }

            _detectedHand.value = DetectedHand(
                boundingBox = handRectF,
                landmarks = pixelLandmarks,
                inputImageWidth = inputImage.width,
                inputImageHeight = inputImage.height
            )

            listener.onHandDetected(handRectF, pixelLandmarks)
        } else {
            _detectedHand.value = null
            listener.onHandDetected(null, null)
        }
    }

    private fun onError(error: RuntimeException) {
        Log.e(TAG, "MediaPipe HandLandmarker error: ${error.message}")
        _detectedHand.value = null
        listener.onHandDetected(null, null)
    }

    fun stop() {
        backgroundExecutor.execute {
            handLandmarker?.close()
            backgroundExecutor.shutdown()
        }
    }

    // Helper function to convert ImageProxy to Bitmap for MediaPipe
    private fun ImageProxy.toBitmap(): Bitmap {
        val bitmapBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Ensure that `use` correctly copies content. This assumes `planes[0].buffer`
        // contains the entire ARGB_8888 content. For YUV_420_888, it's more complex.
        // A more robust conversion for YUV_420_888 to Bitmap:
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, this.width, this.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, yuvImage.width, yuvImage.height), 90, out)
        val imageBytes = out.toByteArray()
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // Rotate the bitmap if necessary to match the display orientation
        val rotationDegrees = this.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle() // Recycle original bitmap
            return rotatedBitmap
        }
        return bitmap
    }

    // NEW: Function to convert a Bitmap to grayscale
    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width = bmpOriginal.width
        val height = bmpOriginal.height
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f) // Set saturation to 0 for grayscale
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        canvas.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    companion object {
        private const val TAG = "HandDetectionAnalyzer"
    }
}
