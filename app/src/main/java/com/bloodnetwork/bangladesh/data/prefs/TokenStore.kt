package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bloodnetwork.bangladesh.data.model.AuthResponse
import com.bloodnetwork.bangladesh.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[Keys.REFRESH_TOKEN] }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { !it[Keys.ACCESS_TOKEN].isNullOrEmpty() }
    val currentUserId: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ID] }
    val currentUserRole: Flow<UserRole?> = context.dataStore.data.map {
        it[Keys.USER_ROLE]?.let { role -> runCatching { UserRole.valueOf(role) }.getOrNull() }
    }

    suspend fun currentAccessToken(): String? = accessToken.first()

    suspend fun saveSession(auth: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = auth.accessToken
            prefs[Keys.REFRESH_TOKEN] = auth.refreshToken
            prefs[Keys.USER_ID] = auth.user.id.toString()
            prefs[Keys.USER_NAME] = auth.user.fullName
            prefs[Keys.USER_PHONE] = auth.user.phoneNumber
            prefs[Keys.USER_ROLE] = auth.user.role.name
        }
    }

    suspend fun updateAccessToken(token: String) {
        context.dataStore.edit { it[Keys.ACCESS_TOKEN] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
