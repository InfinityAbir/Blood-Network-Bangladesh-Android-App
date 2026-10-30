package com.bloodnetwork.bangladesh

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bloodnetwork.bangladesh.data.AppContainer
import com.bloodnetwork.bangladesh.notifications.PushMessagingService

/** Whether any of the app's activities is currently visible (started, not yet stopped). */
object AppVisibility {
    @Volatile
    private var startedActivities = 0

    val isForeground: Boolean get() = startedActivities > 0

    internal fun onStarted() { startedActivities++ }
    internal fun onStopped() { if (startedActivities > 0) startedActivities-- }
}

class BloodNetworkApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PushMessagingService.ensureChannel(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) = AppVisibility.onStarted()
            override fun onActivityStopped(activity: Activity) = AppVisibility.onStopped()
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
