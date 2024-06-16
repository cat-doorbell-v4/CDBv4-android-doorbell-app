package com.example.cdbv4_pixel_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class LogcatService : Service() {

    private lateinit var logDir: File
    private lateinit var currentLogFile: File
    private lateinit var process: Process
    private val maxFileSize = 1 * 1024 * 1024 // 1 MB
    private val maxFileCount = 5

    override fun onCreate() {
        super.onCreate()
        logDir = File(filesDir, "logs")
        if (!logDir.exists()) {
            val dirCreated = logDir.mkdirs()
            Log.i("LogcatService", "Log directory created: $dirCreated")
        } else {
            Log.i("LogcatService", "Log directory exists")
        }
        startLogcatCapture()
    }

    override fun onDestroy() {
        super.onDestroy()
        process.destroy()
    }

    private fun startLogcatCapture() {
        Thread {
            try {
                process = ProcessBuilder().command("logcat").redirectErrorStream(true).start()
                val reader = InputStreamReader(process.inputStream)
                reader.use { input ->
                    var outputStream = createNewLogFile()
                    input.forEachLine { line ->
                        if (currentLogFile.length() >= maxFileSize) {
                            outputStream.close()
                            rotateLogs()
                            outputStream = createNewLogFile()
                        }
                        outputStream.write((line + "\n").toByteArray())
                    }
                }
            } catch (e: Exception) {
                Log.e("LogcatService", "Error capturing logcat", e)
            }
        }.start()
    }

    private fun createNewLogFile(): FileOutputStream {
        currentLogFile = File(logDir, "${Constants.LOG_PREFIX}-${System.currentTimeMillis()}.log")
        Log.i("LogcatService", "New log: $currentLogFile")
        return FileOutputStream(currentLogFile, true)
    }

    private fun rotateLogs() {
        val logFiles = logDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        if (logFiles.size >= maxFileCount) {
            logFiles.drop(maxFileCount - 1).forEach { it.delete() }
            Log.i("LogcatService", "Rotating logs, deleted oldest log files")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
