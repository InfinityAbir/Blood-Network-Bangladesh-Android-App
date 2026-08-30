package com.bloodnetwork.bangladesh

import android.app.Application
import com.bloodnetwork.bangladesh.data.AppContainer
import com.bloodnetwork.bangladesh.notifications.PushMessagingService

class BloodNetworkApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PushMessagingService.ensureChannel(this)
    }
}