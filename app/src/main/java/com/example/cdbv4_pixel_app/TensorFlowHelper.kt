package com.example.cdbv4_pixel_app

import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object TensorFlowHelper {

    private lateinit var hearCatInterpreter: Interpreter
    private lateinit var seeCatInterpreter: Interpreter

    fun init(assetManager: AssetManager) {
        hearCatInterpreter = Interpreter(loadModelFile(assetManager, "yamnet.tflite"))
        seeCatInterpreter = Interpreter(loadModelFile(assetManager, "efficientdet_lite0.tflite"))
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun hearCat(audioBuffer: ByteBuffer): Boolean {
        checkInitialization()
        // Convert audioBuffer to appropriate input format
        val inputBuffer =
            ByteBuffer.allocateDirect(4 * audioBuffer.remaining()).order(ByteOrder.nativeOrder())
        while (audioBuffer.hasRemaining()) {
            inputBuffer.putFloat(audioBuffer.get().toFloat())
        }
        inputBuffer.rewind()

        val outputBuffer = Array(1) { FloatArray(1) }
        hearCatInterpreter.run(inputBuffer, outputBuffer)
        return outputBuffer[0][0] > 0.5
    }

    fun seeCat(bitmap: Bitmap): Boolean {
        checkInitialization()
        // Convert bitmap to appropriate input format
        val inputBuffer = ByteBuffer.allocateDirect(4 * bitmap.width * bitmap.height * 3)
            .order(ByteOrder.nativeOrder())
        val intValues = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in intValues) {
            inputBuffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
            inputBuffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
            inputBuffer.putFloat(((pixel and 0xFF) / 255.0f))
        }
        inputBuffer.rewind()

        val outputBuffer = Array(1) { FloatArray(1) }
        seeCatInterpreter.run(inputBuffer, outputBuffer)
        return outputBuffer[0][0] > 0.5
    }

    private fun checkInitialization() {
        if (!::hearCatInterpreter.isInitialized || !::seeCatInterpreter.isInitialized) {
            throw UninitializedPropertyAccessException("TensorFlow interpreters are not initialized. Call init() first.")
        }
    }
}
