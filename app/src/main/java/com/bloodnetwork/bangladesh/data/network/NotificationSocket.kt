package com.bloodnetwork.bangladesh.data.network

import android.util.Log
import com.bloodnetwork.bangladesh.data.prefs.TokenStore
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class LiveNotification(
    val title: String,
    val message: String,
    val type: String,
    val relatedEntityId: String?,
    val metadata: String?,
)

/**
 * Thin wrapper over the backend's `/hubs/notifications` SignalR hub for realtime
 * notification + unread-count pushes, so the app doesn't have to poll. Started when the
 * user logs in, stopped on logout (see AuthViewModel's isLoggedIn collector) — and
 * re-poked on every app foreground (see MainActivity.onStart) since SignalR's own
 * automatic reconnect eventually gives up if the app sits backgrounded long enough for
 * Android to kill the socket.
 *
 * REST calls (getNotifications/getUnreadCount) never depend on this — this is a live
 * enhancement, not the primary data path, so a dropped socket degrades to "not instant"
 * rather than "broken".
 */
class NotificationSocket(
    baseApiUrl: String,
    private val tokenStore: TokenStore,
) {
    private val hubUrl = baseApiUrl.removeSuffix("/").removeSuffix("/api") + "/hubs/notifications"

    private var connection: HubConnection? = null

    private val _notifications = MutableSharedFlow<LiveNotification>(extraBufferCapacity = 8)
    val notifications = _notifications.asSharedFlow()

    private val _unreadCount = MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 1)
    val unreadCount = _unreadCount.asSharedFlow()

    fun start() {
        if (connection?.connectionState == HubConnectionState.CONNECTED) return

        val hub = HubConnectionBuilder.create(hubUrl)
            .withAccessTokenProvider(Single.defer { Single.just(tokenStore.currentAccessTokenSync().orEmpty()) })
            .withAutomaticReconnect()
            .build()

        hub.onClosed { error ->
            Log.w(TAG, "Notification hub closed${if (error != null) ": ${error.message}" else " (all reconnect attempts exhausted)"}")
            connection = null
        }

        hub.on(
            "ReceiveNotification",
            { payload: NotificationPayload ->
                _notifications.tryEmit(
                    LiveNotification(payload.title, payload.message, payload.type, payload.relatedEntityId, payload.metadata),
                )
            },
            NotificationPayload::class.java,
        )

        hub.on("UnreadCount", { count: Int -> _unreadCount.tryEmit(count) }, Integer::class.java)

        connection = hub
        runCatching {
            hub.start().subscribe(
                { Log.d(TAG, "Connected to notification hub") },
                { e -> Log.w(TAG, "Notification hub connection failed", e) },
            )
        }
    }

    fun stop() {
        runCatching { connection?.stop() }
        connection = null
    }

    /** Field names must match SignalRNotificationBroadcaster's anonymous payload exactly (Gson maps by name). */
    private class NotificationPayload {
        @JvmField var title: String = ""
        @JvmField var message: String = ""
        @JvmField var type: String = ""
        @JvmField var relatedEntityId: String? = null
        @JvmField var metadata: String? = null
    }

    companion object {
        private const val TAG = "NotificationSocket"
    }
}
