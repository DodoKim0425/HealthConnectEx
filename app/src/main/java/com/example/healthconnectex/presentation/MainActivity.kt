package com.example.healthconnectex.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthConnectManager = (application as BaseApplication).healthConnectManager

        setContent {
            HealthConnectApp(healthConnectManager = healthConnectManager)
        }
    }
}
