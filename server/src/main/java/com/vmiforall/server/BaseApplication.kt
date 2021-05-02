package com.vmiforall.server

import android.app.Application
import android.content.Intent
import com.vmiforall.server.services.RemoteControlService
import dagger.hilt.android.HiltAndroidApp

// used in the manifest
@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Intent(this, RemoteControlService::class.java).also { intent ->
            startService(intent)
        }
    }
}
