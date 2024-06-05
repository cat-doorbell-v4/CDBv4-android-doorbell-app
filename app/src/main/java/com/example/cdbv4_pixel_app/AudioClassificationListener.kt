package com.example.cdbv4_pixel_app

import org.tensorflow.lite.support.label.Category

interface AudioClassificationListener {
    fun onError(error: String)
    fun onResult(results: List<Category>, inferenceTime: Long)
}
