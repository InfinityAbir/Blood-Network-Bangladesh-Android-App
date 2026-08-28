package com.bloodnetwork.bangladesh

import android.app.Application
import com.bloodnetwork.bangladesh.data.AppContainer

class BloodNetworkApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
