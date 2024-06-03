package com.example.cdbv4_pixel_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cdbv4_pixel_app.statemachine.StateMachine

class MainActivity : AppCompatActivity() {

    private lateinit var stateMachine: StateMachine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stateMachine = StateMachine(this)
        stateMachine.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stateMachine.stop()
    }
}
