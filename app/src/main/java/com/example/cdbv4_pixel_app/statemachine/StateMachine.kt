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
    private var isWaitingScheduled = false

    private var soundDetectionService: SoundDetectionService? = null
    private var cameraService: CameraService? = null
    private var flashlightService: FlashlightService? = null
    private var notificationService: NotificationService? = null

    private val tag = "StateMachine"

    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        Log.i(tag, "Starting state machine")
        transitionTo(State.LISTENING)
    }

    fun stop() {
        Log.i(tag, "Stopping state machine")
        handler.removeCallbacksAndMessages(null)
        isWaitingScheduled = false
    }

    private fun onCatHeard() {
        Log.i(tag, "Cat heard, transitioning to LOOKING")
        transitionTo(State.LOOKING)
    }

    private fun onCatSeen(seen: Boolean) {
        if (seen) {
            Log.i(tag, "Cat seen, transitioning to NOTIFYING")
            transitionTo(State.NOTIFYING)
        } else {
            Log.i(tag, "No cat seen, transitioning back to LISTENING")
            transitionTo(State.LISTENING)
        }
    }

    private fun onNotificationSent() {
        Log.i(tag, "Notification sent, transitioning to WAITING")
        transitionTo(State.WAITING)
    }

    private fun isLowLight(): Boolean {
        // Implement low-light detection logic
        return false
    }

    private fun transitionTo(newState: State) {
        Log.i(tag, "Transitioning from $currentState to $newState")

        when (newState) {
            State.LISTENING -> {
                Log.i(tag, "Entering LISTENING state")
                currentState = State.LISTENING
                soundDetectionService = SoundDetectionService { onCatHeard() }
                soundDetectionService?.initialize(context)
                soundDetectionService?.startListening()
                isWaitingScheduled = false
                Log.i(tag, "Exiting LISTENING state")
            }
            State.LOOKING -> {
                Log.i(tag, "Entering LOOKING state")
                currentState = State.LOOKING
                soundDetectionService?.stopListening()
                soundDetectionService = null

                flashlightService = FlashlightService(context)
                if (isLowLight()) {
                    flashlightService?.turnOn()
                }

                cameraService = CameraService(context) { onCatSeen(it) }
                cameraService?.startCamera()
                Log.i(tag, "Exiting LOOKING state")
            }
            State.NOTIFYING -> {
                Log.i(tag, "Entering NOTIFYING state")
                currentState = State.NOTIFYING

                cameraService?.stopCamera()
                cameraService = null

                flashlightService?.turnOff()
                flashlightService = null

                notificationService = NotificationService(context) { onNotificationSent() }
                notificationService?.sendNotification()
                Log.i(tag, "Exiting NOTIFYING state")
            }
            State.WAITING -> {
                notificationService = null
                if (currentState != State.WAITING) {
                    Log.i(tag, "Entering WAITING state")
                    currentState = State.WAITING
                    if (!isWaitingScheduled) {
                        isWaitingScheduled = true
                        handler.postDelayed({
                            Log.i(tag, "WAITING state over, transitioning to LISTENING")
                            transitionTo(State.LISTENING)
                        }, 2 * 60 * 1000) // 2 minutes delay
                        Log.i(tag, "Handler posted for WAITING state")
                    }
                }
            }
        }
    }
}
