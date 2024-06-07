package com.example.cdbv4_pixel_app.services

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraService(private val context: Context, private val onCatSeen: (Boolean) -> Unit) :
    ObjectDetectorHelper.DetectorListener {

    private val TAG = "CameraService"

    private var objectDetectorHelper: ObjectDetectorHelper = ObjectDetectorHelper(
        context = context,
        objectDetectorListener = this
    )
    private lateinit var bitmapBuffer: Bitmap
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraBound: Boolean = false
    private var cameraStopped: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    init {
        // setupCamera() is not called here anymore
    }

    @SuppressLint("MissingPermission")
    private fun setupCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases() // Ensure we try to bind when the camera provider is ready
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing camera provider: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun checkFlashNotificationSetting() {
        try {
            val flashNotificationSetting =
                Settings.System.getInt(context.contentResolver, "camera_flash_notification")
            Log.i(TAG, "Camera flash notification setting: $flashNotificationSetting")
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(TAG, "Camera flash notification setting not found: ${e.message}")
            // Provide fallback mechanism or alternative action here
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        checkFlashNotificationSetting()

        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        val previewBuilder = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)

        // Configure Camera2Interop with session capture callback timeout
        val camera2Interop = Camera2Interop.Extender(previewBuilder)
        camera2Interop.setCaptureRequestOption(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        camera2Interop.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON
        )
        camera2Interop.setCaptureRequestOption(
            CaptureRequest.CONTROL_AWB_MODE,
            CaptureRequest.CONTROL_AWB_MODE_AUTO
        )
        camera2Interop.setCaptureRequestOption(
            CaptureRequest.SENSOR_FRAME_DURATION,
            10000000L // Set timeout to 10 seconds (10,000,000 nanoseconds)
        )

        val preview = previewBuilder.build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { image ->
                    if (!::bitmapBuffer.isInitialized) {
                        bitmapBuffer =
                            Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                    }
                    detectObjects(image)
                }
            }

        try {
            if (cameraBound) {
                Log.i(TAG, "Unbinding all use cases")
                cameraProvider.unbindAll()
            }

            Log.i(TAG, "Binding to lifecycle")
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            Log.i(TAG, "Camera bound")
            cameraBound = true
            cameraStopped = false
        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera use cases: ${e.message}")
        }
    }

    private fun detectObjects(image: ImageProxy) {
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        val imageRotation = image.imageInfo.rotationDegrees
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        if (cameraStopped) {
            Log.i(TAG, "Camera stopped, ignoring results")
            return
        }

        var catDetected = false

        timeoutRunnable = Runnable {
            if (!catDetected) {
                Log.i(TAG, "No cat detected within 45 seconds. Giving up.")
                handler.removeCallbacks(timeoutRunnable!!)
                stopCameraProcessing()
                onCatSeen(false)
            }
        }

        handler.postDelayed(timeoutRunnable!!, 45000) // 45 seconds

        if (results != null) {
            loop@ for (detection in results) {
                for (category in detection.categories) {
                    Log.i(TAG, "Detected label: ${category.label}")
                    if (category.label.equals("cat", ignoreCase = true)) {
                        Log.d(TAG, "Cat seen!")
                        catDetected = true
                        handler.removeCallbacks(timeoutRunnable!!)
                        stopCameraProcessing()
                        onCatSeen(true)
                        break@loop
                    }
                }
            }
        }
    }

    override fun onError(error: String) {
        Log.e(TAG, "Object detection error: $error")
    }

    fun startCamera() {
        if (!cameraBound) {
            Log.i(TAG, "Starting camera")
            setupCamera() // Setup camera when starting it
        } else {
            Log.e(TAG, "Camera already bound")
        }
    }

    fun stopCamera() {
        Log.i(TAG, "Stopping camera")
        stopCameraProcessing()
        Handler(Looper.getMainLooper()).post {
            cameraProvider?.unbindAll()
        }
        cameraBound = false
    }

    private fun stopCameraProcessing() {
        cameraStopped = true
        timeoutRunnable?.let { handler.removeCallbacks(it) }
    }
}
