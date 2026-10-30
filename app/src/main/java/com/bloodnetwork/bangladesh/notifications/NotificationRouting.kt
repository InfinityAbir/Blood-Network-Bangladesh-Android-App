package com.bloodnetwork.bangladesh.notifications

import com.bloodnetwork.bangladesh.ui.navigation.Routes

/**
 * Maps a push notification (type + related entity id) to the screen it should open.
 * Mirrors what tapping the same notification does inside NotificationsScreen.
 *
 * MainActivity is exported, so the id that reaches here may come from any app: only a
 * UUID-shaped id is ever put into a route, and the result is always one of a fixed set.
 */
object NotificationRouting {

    private val UUID_SHAPE = Regex("^[0-9a-fA-F-]{36}$")

    fun routeFor(type: String?, relatedEntityId: String?): String = when (type) {
        "Availability" -> Routes.FIND_BLOOD
        "BloodRequestMatch" -> Routes.MY_MATCHES
        "DonorAccepted", "DonorDeclined" ->
            relatedEntityId?.takeIf { UUID_SHAPE.matches(it) }
                ?.let { "${Routes.REQUEST_DETAILS}/$it" }
                ?: Routes.NOTIFICATIONS
        else -> Routes.NOTIFICATIONS
    }
}
