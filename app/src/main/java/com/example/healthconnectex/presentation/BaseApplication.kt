package com.example.healthconnectex.presentation

import android.app.Application
import com.example.healthconnectex.data.HealthConnectManager


class BaseApplication : Application() {
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }
}