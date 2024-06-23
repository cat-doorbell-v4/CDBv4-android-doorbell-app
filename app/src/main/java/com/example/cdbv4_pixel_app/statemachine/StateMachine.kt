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

    private var currentState: State = State.INIT
    private var soundDetectionService: SoundDetectionService? = null
    private var cameraService: CameraService? = null
    private var notificationService: NotificationService? = null

    private val tag = "StateMachine"

    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        Log.i(tag, "Starting state machine")
        transitionTo(State.LISTEN)
    }

    fun stop() {
        Log.i(tag, "Stopping state machine")
        handler.removeCallbacksAndMessages(null)
    }

    private fun onCatHeard() {
        Log.i(tag, "Cat heard, transitioning to LOOK")
        if (currentState == State.LISTEN) {
            transitionTo(State.LOOK)
        } else {
            Log.w(tag, "Ignored cat heard event while in state: $currentState")
        }
    }

    private fun onCatSeen(seen: Boolean) {
        if (seen) {
            Log.i(tag, "Cat seen, transitioning to RING")
            if (currentState == State.LOOK) {
                transitionTo(State.RING)
            } else {
                Log.w(tag, "Ignored cat seen event while in state: $currentState")
            }
        } else {
            Log.i(tag, "No cat seen, transitioning back to LISTEN")
            if (currentState == State.LOOK) {
                transitionTo(State.LISTEN)
            } else {
                Log.w(tag, "Ignored no cat seen event while in state: $currentState")
            }
        }
    }

    private fun onNotificationSent() {
        Log.i(tag, "Notification sent, scheduling return to LISTEN")
        if (currentState == State.RING) {
            handler.postDelayed({
                Log.i(tag, "Returning to LISTEN after notification")
                transitionTo(State.LISTEN)
            }, 2 * 60 * 1000) // 2 minutes delay
        } else {
            Log.w(tag, "Ignored notification sent event while in state: $currentState")
        }
    }

    private fun scheduleHeartbeat() {
        handler.postDelayed({
            if (currentState == State.LISTEN) {
                MyApplication.deviceName?.let {
                    notificationService?.sendNotification("heartbeat")
                }
                Log.i(tag, "Heartbeat notification sent")
                scheduleHeartbeat() // Reschedule the heartbeat
            }
        }, 10 * 60 * 1000) // 10 minutes delay
    }

    private fun transitionTo(newState: State) {
        Log.i(tag, "Transitioning from $currentState to $newState")

        if (currentState == newState) {
            Log.w(tag, "Already in state: $newState, ignoring transition")
            return
        }

        when (newState) {
            State.LISTEN -> {
                Log.i(tag, "Entering LISTEN state")
                currentState = State.LISTEN
                soundDetectionService = SoundDetectionService { onCatHeard() }
                soundDetectionService?.initialize(context)
                soundDetectionService?.startListening()
                Log.i(tag, "Sound detection service started")
                scheduleHeartbeat()
                Log.i(tag, "Heartbeat scheduled")
            }

            State.LOOK -> {
                Log.i(tag, "Entering LOOK state")
                currentState = State.LOOK
                soundDetectionService?.stopListening()
                soundDetectionService = null
                cameraService = CameraService(context) { onCatSeen(it) }
                cameraService?.startCamera()
                Log.i(tag, "Exiting LOOK state")
            }

            State.RING -> {
                Log.i(tag, "Entering RING state")
                currentState = State.RING

                cameraService?.stopCamera()
                cameraService = null

                notificationService = NotificationService(context) { onNotificationSent() }
                MyApplication.deviceName?.let { notificationService?.sendNotification("ring") }
                Log.i(tag, "Exiting RING state")
            }

            State.INIT -> {
                // Do nothing, just a placeholder state
            }
        }
    }
}
