package com.example.cdbv4_pixel_app.statemachine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cdbv4_pixel_app.MyApplication
import com.example.cdbv4_pixel_app.services.CameraService
import com.example.cdbv4_pixel_app.services.NotificationService
import com.example.cdbv4_pixel_app.services.SoundDetectionService

class StateMachine(private val context: Context) {

    private var currentState: State = State.LISTENING
    private var isWaitingScheduled = false

    private var soundDetectionService: SoundDetectionService? = null
    private var cameraService: CameraService? = null
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
            Log.i(tag, "Cat seen, transitioning to RINGING")
            transitionTo(State.RINGING)
        } else {
            Log.i(tag, "No cat seen, transitioning back to LISTENING")
            transitionTo(State.LISTENING)
        }
    }

    private fun onNotificationSent() {
        Log.i(tag, "Notification sent, transitioning to PAUSING")
        transitionTo(State.PAUSING)
    }

    private fun scheduleHeartbeat() {
        handler.postDelayed({
            if (currentState == State.LISTENING) {
                transitionTo(State.BEATING)
            }
        }, 10 * 60 * 1000) // 10 minutes delay
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
                scheduleHeartbeat()
                isWaitingScheduled = false
                Log.i(tag, "Exiting LISTENING state")
            }
            State.LOOKING -> {
                Log.i(tag, "Entering LOOKING state")
                currentState = State.LOOKING
                soundDetectionService?.stopListening()
                soundDetectionService = null
                cameraService = CameraService(context) { onCatSeen(it) }
                cameraService?.startCamera()
                Log.i(tag, "Exiting LOOKING state")
            }
            State.RINGING -> {
                Log.i(tag, "Entering RINGING state")
                currentState = State.RINGING

                cameraService?.stopCamera()
                cameraService = null

                notificationService = NotificationService(context) { onNotificationSent() }
                MyApplication.deviceName?.let { notificationService?.sendNotification("ring", it) }
                Log.i(tag, "Exiting RINGING state")
            }
            State.PAUSING -> {
                notificationService = null
                if (currentState != State.PAUSING) {
                    Log.i(tag, "Entering PAUSING state")
                    currentState = State.PAUSING
                    if (!isWaitingScheduled) {
                        isWaitingScheduled = true
                        handler.postDelayed({
                            Log.i(tag, "PAUSING state over, transitioning to LISTENING")
                            transitionTo(State.LISTENING)
                        }, 2 * 60 * 1000) // 2 minutes delay
                        Log.i(tag, "Handler posted for PAUSING state")
                    }
                }
            }
            State.BEATING -> {
                Log.i(tag, "Entering BEATING state")
                currentState = State.BEATING
                notificationService = NotificationService(context) {
                    Log.i(tag, "Heartbeat notification sent")
                    transitionTo(State.LISTENING)
                }
                MyApplication.deviceName?.let {
                    notificationService?.sendNotification(
                        "heartbeat",
                        it
                    )
                }
                Log.i(tag, "Exiting BEATING state")
            }
        }
    }
}
