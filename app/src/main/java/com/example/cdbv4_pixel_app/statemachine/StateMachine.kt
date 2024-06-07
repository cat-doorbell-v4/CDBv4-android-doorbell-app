package com.example.cdbv4_pixel_app.statemachine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cdbv4_pixel_app.services.CameraService
import com.example.cdbv4_pixel_app.services.FlashlightService
import com.example.cdbv4_pixel_app.services.NotificationService
import com.example.cdbv4_pixel_app.services.SoundDetectionService

class StateMachine(private val context: Context) {

    private var currentState: State = State.LISTENING

    private val soundDetectionService = SoundDetectionService { onCatHeard() }
    private val cameraService = CameraService(context) { onCatSeen(it) }
    private val flashlightService = FlashlightService(context)
    private val notificationService = NotificationService(context) { onNotificationSent() }

    private val tag = "StateMachine"

    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        soundDetectionService.initialize(context)
        soundDetectionService.startListening()
    }

    fun stop() {
        soundDetectionService.stopListening()
        cameraService.stopCamera()
        flashlightService.turnOff()
        handler.removeCallbacksAndMessages(null)
    }

    private fun onCatHeard() {
        transitionTo(State.CAPTURING)
    }

    private fun onCatSeen(seen: Boolean) {
        if (seen) {
            transitionTo(State.NOTIFYING)
        } else {
            transitionTo(State.WAITING)
        }
    }

    private fun onNotificationSent() {
        transitionTo(State.WAITING)
    }

    private fun isLowLight(): Boolean {
        // Implement low-light detection logic
        return false
    }

    private fun transitionTo(newState: State) {
        when (newState) {
            State.LISTENING -> {
                Log.i(tag, "Listening")
                currentState = State.LISTENING
                cameraService.stopCamera()
                flashlightService.turnOff()
                soundDetectionService.startListening()
            }

            State.CAPTURING -> {
                Log.i(tag, "Capturing")
                currentState = State.CAPTURING
                soundDetectionService.stopListening()
                if (isLowLight()) flashlightService.turnOn()
                cameraService.startCamera()
            }

            State.NOTIFYING -> {
                Log.i(tag, "Notifying")
                currentState = State.NOTIFYING
                cameraService.stopCamera()
                flashlightService.turnOff()
                notificationService.sendNotification()
            }

            State.WAITING -> {
                Log.i(tag, "Waiting")
                currentState = State.WAITING
                soundDetectionService.stopListening()
                cameraService.stopCamera()
                flashlightService.turnOff()
                handler.postDelayed({
                    transitionTo(State.LISTENING)
                }, 2 * 60 * 1000) // 2 minutes in milliseconds
            }
        }
    }
}
