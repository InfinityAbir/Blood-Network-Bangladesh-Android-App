package com.bloodnetwork.bangladesh.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bloodnetwork.bangladesh.AppVisibility
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
import java.util.concurrent.atomic.AtomicInteger

/**
 * FCM plumbing: picks up fresh registration tokens and renders OS-level
 * notifications for backgrounded/killed-app delivery (in-app delivery is
 * handled by the SignalR [NotificationSocket]).
 */
class PushMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "FCM issued new token")
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
        Log.i(TAG, "FCM message received: id=${message.messageId} notification=${message.notification != null} data=${message.data.keys}")

        // The app is open and its live SignalR connection already delivers this event in-app
        // (badge + list). A second, system-tray copy would just be a duplicate. If the socket
        // is down we still post it, so a foreground user never silently misses an alert.
        val app = application as? BloodNetworkApp
        if (AppVisibility.isForeground && app?.container?.notificationSocket?.isConnected == true) {
            Log.i(TAG, "App in foreground with live socket — skipping system notification")
            return
        }

        // Fall back to the data payload when there's no notification block: a data-only
        // message (or one the system tray didn't render) still deserves to be shown.
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["message"] ?: ""
        if (title.isEmpty() && body.isEmpty()) {
            Log.w(TAG, "FCM message had no renderable title/body — dropping")
            return
        }
        showNotification(
            title = title,
            body = body,
            type = message.data["type"],
            relatedEntityId = message.data["relatedEntityId"]?.takeIf { it.isNotBlank() },
            notificationId = notificationIdFor(message.messageId),
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        type: String?,
        relatedEntityId: String?,
        notificationId: Int,
    ) {
        // `notificationId` is unique per push (see notificationIdFor): a shared id made each new
        // alert replace the previous one. It doubles as the PendingIntent request code so every
        // notification keeps its own tap destination (FLAG_UPDATE_CURRENT on a shared request
        // code would overwrite earlier extras).

        val contentIntent = PendingIntent.getActivity(
            this,
            notificationId,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_OPEN_NOTIFICATIONS
                putExtra(EXTRA_TYPE, type)
                putExtra(EXTRA_RELATED_ID, relatedEntityId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setContentIntent(contentIntent)
            .setColor(0xFFD32F2F.toInt())

        // Collapses several alerts into one expandable stack instead of a long list.
        val summary = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setColor(0xFFD32F2F.toInt())

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(this)
        manager.notify(notificationId, builder.build())
        manager.notify(SUMMARY_NOTIFICATION_ID, summary.build())
        Log.i(TAG, "Posted notification to channel $CHANNEL_ID")
    }

    companion object {
        private const val TAG = "PushMessaging"
        const val CHANNEL_ID = "blood_updates"
        const val ACTION_OPEN_NOTIFICATIONS = "com.bloodnetwork.bangladesh.OPEN_NOTIFICATIONS"
        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_RELATED_ID = "notification_related_id"
        private const val GROUP_KEY = "com.bloodnetwork.bangladesh.UPDATES"
        private const val SUMMARY_NOTIFICATION_ID = 1000
        private const val MIN_NOTIFICATION_ID = 2000
        private const val ID_RANGE = 1_000_000_000

        // Process-local fallback for messages that carry no id. Seeded from the clock because a
        // plain counter restarts at the same value in every new process (Android kills and
        // re-creates this app between pushes), which made a later push replace an earlier one.
        private val fallbackId = AtomicInteger((System.currentTimeMillis() / 1000 % ID_RANGE).toInt())

        /**
         * Stable and unique per FCM message across processes (and a redelivered message maps to
         * the same id, so it can't appear twice). Always >= MIN_NOTIFICATION_ID so it can never
         * collide with the group summary.
         */
        private fun notificationIdFor(messageId: String?): Int {
            val raw = messageId?.hashCode() ?: fallbackId.incrementAndGet()
            return (raw and Int.MAX_VALUE) % ID_RANGE + MIN_NOTIFICATION_ID
        }

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
