package com.bloodnetwork.bangladesh.ui.util

import android.content.Context
import com.bloodnetwork.bangladesh.R

/** Public legal / support endpoints (configured in strings.xml so they're one edit away). */
object AppLinks {
    fun privacyPolicyUrl(context: Context): String = context.getString(R.string.privacy_policy_url)
    fun accountDeletionUrl(context: Context): String = context.getString(R.string.account_deletion_url)
    fun supportEmail(context: Context): String = context.getString(R.string.support_email)

    /**
     * Address an avatar may be loaded from, or null. Profile pictures are served by our own API, so
     * a relative `/api/photos/...` path and anything under our own API address are trusted (this is
     * also what lets the local-test build load pictures over plain HTTP). Any other value is
     * user-controlled data and must be a plain https URL.
     */
    fun resolveImageUrl(context: Context, raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        val apiBase = context.getString(R.string.base_api_url).trimEnd('/')
        return when {
            value.startsWith("/api/photos/") -> apiBase + value
            value.startsWith("$apiBase/api/photos/") -> value
            else -> SafeLinks.httpsUrlOrNull(value)
        }
    }
}
