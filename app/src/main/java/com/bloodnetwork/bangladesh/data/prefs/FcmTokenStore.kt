package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context

/**
 * Stores the device's FCM registration token plus the userId it was last
 * registered against, so we don't re-upload the same token every session.
 * Kept separate from the encrypted [TokenStore] (token is not secret).
 */
class FcmTokenStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("fcm_registration", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "fcm_token"
        private const val KEY_USER_ID = "fcm_user_id"
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun setToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getRegisteredUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun setRegistration(token: String, userId: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
}