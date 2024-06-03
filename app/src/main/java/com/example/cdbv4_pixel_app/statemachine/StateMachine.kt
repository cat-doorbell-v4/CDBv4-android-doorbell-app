package com.example.cdbv4_pixel_app.statemachine

import android.content.Context
import com.example.cdbv4_pixel_app.services.CameraService
import com.example.cdbv4_pixel_app.services.FlashlightService
import com.example.cdbv4_pixel_app.services.NotificationService
import com.example.cdbv4_pixel_app.services.SoundDetectionService

class StateMachine(private val context: Context) {

    private var currentState: State = State.LISTENING

    private val soundDetectionService = SoundDetectionService(context) { onMeowDetected() }
    private val cameraService = CameraService(context) { onCatDetected(it) }
    private val flashlightService = FlashlightService(context)
    private val notificationService = NotificationService(context)

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
                currentState = State.LISTENING
                soundDetectionService.startListening()
                cameraService.stopCamera()
                flashlightService.turnOff()
            }

            State.CAPTURING -> {
                currentState = State.CAPTURING
                soundDetectionService.stopListening()
                if (isLowLight()) flashlightService.turnOn()
                cameraService.startCamera()
            }

            State.NOTIFYING -> {
                currentState = State.NOTIFYING
                notificationService.sendNotification { onNotificationSent() }
            }

            State.WAITING -> {
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
