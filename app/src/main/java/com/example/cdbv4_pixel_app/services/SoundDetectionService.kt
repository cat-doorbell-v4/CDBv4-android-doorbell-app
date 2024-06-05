package com.example.cdbv4_pixel_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.cdbv4_pixel_app.AudioClassificationHelper
import com.example.cdbv4_pixel_app.AudioClassificationListener
import org.tensorflow.lite.support.label.Category

class SoundDetectionService : Service() {
    private lateinit var audioHelper: AudioClassificationHelper

    override fun onCreate() {
        super.onCreate()
        audioHelper = AudioClassificationHelper(
            context = this,
            listener = object : AudioClassificationListener {
                override fun onError(error: String) {
                    Log.e("SoundDetectionService", error)
                }

                override fun onResult(results: List<Category>, inferenceTime: Long) {
                    val isCatMeowDetected =
                        results.any { it.label == "cat_meow" && it.score >= 0.3f }
                    if (isCatMeowDetected) {
                        // Handle cat meow detection, e.g., activate camera, send alert, etc.
                        Log.i("SoundDetectionService", "Heard cat meow!")
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
        audioHelper.startAudioClassification()
    }

    fun stopListening() {
        audioHelper.stopAudioClassification()
    }
}
