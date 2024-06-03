package com.example.cdbv4_pixel_app

import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import kotlin.concurrent.thread

class SoundDetectionService : Service() {

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        thread(start = true) {
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize
            )

            audioRecord.startRecording()

            val audioBuffer = ShortArray(bufferSize)
            while (true) {
                audioRecord.read(audioBuffer, 0, bufferSize)
                val meowDetected = TensorFlowHelper.detectCatMeow(audioBuffer)
                if (meowDetected) {
                    val cameraIntent = Intent(this, CameraXManager::class.java)
                    startService(cameraIntent)
                    break
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
