package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bloodnetwork.bangladesh.data.model.AuthResponse
import com.bloodnetwork.bangladesh.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Access/refresh tokens and session identity, encrypted at rest via the Android Keystore
 * (AES-256-GCM values, AES-256-SIV keys) instead of plaintext SharedPreferences/DataStore.
 */
class TokenStore(context: Context) {

    private object Keys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_PHONE = "user_phone"
        const val USER_ROLE = "user_role"
    }

    private data class State(
        val accessToken: String? = null,
        val refreshToken: String? = null,
        val userId: String? = null,
        val userRole: String? = null,
    )

    private val prefs: SharedPreferences = run {
        // One-time cleanup: earlier versions kept tokens in a plaintext DataStore file
        // named "auth". Remove it so upgrading installs don't leave stale plaintext
        // tokens on disk alongside the new encrypted store.
        runCatching { java.io.File(context.filesDir, "datastore/auth.preferences_pb").delete() }

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "auth_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val state = MutableStateFlow(
        State(
            accessToken = prefs.getString(Keys.ACCESS_TOKEN, null),
            refreshToken = prefs.getString(Keys.REFRESH_TOKEN, null),
            userId = prefs.getString(Keys.USER_ID, null),
            userRole = prefs.getString(Keys.USER_ROLE, null),
        )
    )

    val accessToken: Flow<String?> = state.map { it.accessToken }
    val refreshToken: Flow<String?> = state.map { it.refreshToken }
    val isLoggedIn: Flow<Boolean> = state.map { !it.accessToken.isNullOrEmpty() }
    val currentUserId: Flow<String?> = state.map { it.userId }
    val currentUserRole: Flow<UserRole?> = state.map {
        it.userRole?.let { role -> runCatching { UserRole.valueOf(role) }.getOrNull() }
    }

    suspend fun currentAccessToken(): String? = accessToken.first()

    fun currentAccessTokenSync(): String? = state.value.accessToken

    fun isLoggedInSync(): Boolean = !state.value.accessToken.isNullOrEmpty()

    fun currentUserRoleSync(): UserRole? =
        state.value.userRole?.let { role -> runCatching { UserRole.valueOf(role) }.getOrNull() }

    suspend fun saveSession(auth: AuthResponse) {
        prefs.edit()
            .putString(Keys.ACCESS_TOKEN, auth.accessToken)
            .putString(Keys.REFRESH_TOKEN, auth.refreshToken)
            .putString(Keys.USER_ID, auth.user.id.toString())
            .putString(Keys.USER_NAME, auth.user.fullName)
            .putString(Keys.USER_PHONE, auth.user.phoneNumber)
            .putString(Keys.USER_ROLE, auth.user.role.name)
            .apply()
        state.value = State(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId = auth.user.id.toString(),
            userRole = auth.user.role.name,
        )
    }

    suspend fun updateAccessToken(token: String) {
        prefs.edit().putString(Keys.ACCESS_TOKEN, token).apply()
        state.value = state.value.copy(accessToken = token)
    }

    suspend fun clear() {
        prefs.edit().clear().apply()
        state.value = State()
    }
}
