package com.example.cdbv4_pixel_app.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.cdbv4_pixel_app.AudioClassificationHelper
import com.example.cdbv4_pixel_app.AudioClassificationListener
import org.tensorflow.lite.support.label.Category

class SoundDetectionService(private val onCatHeardCallback: () -> Unit) : Service() {

    private var audioHelper: AudioClassificationHelper? = null
    private var TAG = "SoundDetectionService"

    override fun onCreate() {
        super.onCreate()
        initialize(this)
    }

    fun initialize(context: Context) {
        audioHelper = AudioClassificationHelper(
            context = context,
            listener = object : AudioClassificationListener {
                override fun onError(error: String) {
                    Log.e(TAG, error)
                }

                override fun onResult(results: List<Category>, inferenceTime: Long) {
//                    results.forEach { category ->
//                        Log.i(TAG, "Label: ${category.label}, Score: ${category.score}")
//                    }

                    val isCatMeowDetected = results.any { it.label == "Cat" && it.score >= 0.8f }
                    if (isCatMeowDetected) {
                        // Handle cat meow detection, e.g., activate camera, send alert, etc.
                        Log.i(TAG, "Heard cat meow!")
                        onCatHeardCallback()
                    }
                }
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startListening()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun startListening() {
        audioHelper?.startAudioClassification()
    }

    fun stopListening() {
        audioHelper?.stopAudioClassification()
    }
}
