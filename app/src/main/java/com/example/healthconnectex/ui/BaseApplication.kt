package com.example.healthconnectex.ui

import android.app.Application
import com.example.healthconnectex.data.HealthConnectManager

class BaseApplication : Application() {
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }
}