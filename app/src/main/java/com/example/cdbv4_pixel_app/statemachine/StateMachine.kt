package com.example.cdbv4_pixel_app.statemachine

import android.content.Context
import android.util.Log
import com.example.cdbv4_pixel_app.services.CameraService
import com.example.cdbv4_pixel_app.services.FlashlightService
import com.example.cdbv4_pixel_app.services.NotificationService
import com.example.cdbv4_pixel_app.services.SoundDetectionService

class StateMachine(private val context: Context) {

    private var currentState: State = State.LISTENING

    private val soundDetectionService = SoundDetectionService()
    private val cameraService = CameraService(context) { onCatDetected(it) }
    private val flashlightService = FlashlightService(context)
    private val notificationService = NotificationService(context)

    private val tag = "StateMachine"

    fun start() {
        soundDetectionService.startListening()
    }

    fun stop() {
        soundDetectionService.stopListening()
        cameraService.stopCamera()
        flashlightService.turnOff()
    }

    private fun onMeowDetected() {
        transitionTo(State.CAPTURING)
    }

    private fun onCatDetected(catDetected: Boolean) {
        if (catDetected) {
            transitionTo(State.NOTIFYING)
        } else {
            transitionTo(State.LISTENING)
        }
    }

    private fun transitionTo(newState: State) {
        when (newState) {
            State.LISTENING -> {
                Log.i(tag, "Listening")
                currentState = State.LISTENING
                soundDetectionService.startListening()
                cameraService.stopCamera()
                flashlightService.turnOff()
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
                notificationService.sendNotification { onNotificationSent() }
            }
            State.WAITING -> {
                Log.i(tag, "Waiting")
                currentState = State.WAITING
                // Implement waiting logic, e.g., a delay of 2 minutes before transitioning back to LISTENING
                transitionTo(State.LISTENING)
            }
        }
    }

    private fun onNotificationSent() {
        transitionTo(State.WAITING)
    }

    private fun isLowLight(): Boolean {
        // Implement low-light detection logic
        return true
    }
}
