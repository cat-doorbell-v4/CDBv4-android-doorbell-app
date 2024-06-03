package com.example.cdbv4_pixel_app

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
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

    fun hearCat(audioBuffer: ShortArray): Boolean {
        // Preprocess and run the model
        val inputBuffer = ... // Convert audioBuffer to appropriate input format
        val outputBuffer = Array(1) { FloatArray(1) }
        hearCatInterpreter.run(inputBuffer, outputBuffer)
        return outputBuffer[0][0] > 0.5
    }

    fun seeCat(bitmap: Bitmap): Boolean {
        // Preprocess and run the model
        val inputBuffer = ... // Convert bitmap to appropriate input format
        val outputBuffer = Array(1) { FloatArray(1) }
        seeCatInterpreter.run(inputBuffer, outputBuffer)
        return outputBuffer[0][0] > 0.5
    }
}
