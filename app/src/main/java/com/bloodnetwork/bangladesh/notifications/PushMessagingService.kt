package com.bloodnetwork.bangladesh.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bloodnetwork.bangladesh.BloodNetworkApp
import com.bloodnetwork.bangladesh.MainActivity
import com.bloodnetwork.bangladesh.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * FCM plumbing: picks up fresh registration tokens and renders OS-level
 * notifications for backgrounded/killed-app delivery (in-app delivery is
 * handled by the SignalR [NotificationSocket]).
 */
class PushMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = application as? BloodNetworkApp ?: return
        app.container.fcmTokenStore.setToken(token)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val userId = app.container.tokenStore.currentUserId.first()
            if (!userId.isNullOrEmpty()) {
                app.container.repository.registerFcmToken(token, userId)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notification = message.notification ?: return
        showNotification(notification.title ?: getString(R.string.app_name), notification.body ?: "")
    }

    private fun showNotification(title: String, body: String) {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_OPEN_NOTIFICATIONS
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setColor(0xFFD32F2F.toInt())

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "blood_updates"
        const val ACTION_OPEN_NOTIFICATIONS = "com.bloodnetwork.bangladesh.OPEN_NOTIFICATIONS"
        private const val NOTIFICATION_ID = 1001

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Blood request & donor updates",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Blood request updates, donor matches, and system notices"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}