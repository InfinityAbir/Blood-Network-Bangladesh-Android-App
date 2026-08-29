package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Earlier versions stored the raw password under this key. Kept here only so save()
// can scrub it from any store an existing install may already have on disk.
private val LEGACY_PASSWORD_KEY = stringPreferencesKey("password")

private val Context.registrationDataStore by preferencesDataStore(name = "registration")

/**
 * Persists in-progress registration fields so the form survives process death.
 * The password is deliberately never stored here — only kept in the screen's
 * in-memory state — so it never lands on disk before the account exists.
 */
class RegistrationStore(private val context: Context) {

    private object Keys {
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
    }

    data class RegistrationData(
        val firstName: String = "",
        val lastName: String = "",
        val phoneNumber: String = "",
    )

    val data: Flow<RegistrationData> = context.registrationDataStore.data.map { prefs ->
        RegistrationData(
            firstName = prefs[Keys.FIRST_NAME] ?: "",
            lastName = prefs[Keys.LAST_NAME] ?: "",
            phoneNumber = prefs[Keys.PHONE_NUMBER] ?: "",
        )
    }

    suspend fun save(data: RegistrationData) {
        context.registrationDataStore.edit { prefs ->
            prefs[Keys.FIRST_NAME] = data.firstName
            prefs[Keys.LAST_NAME] = data.lastName
            prefs[Keys.PHONE_NUMBER] = data.phoneNumber
            prefs.remove(LEGACY_PASSWORD_KEY)
        }
    }

    suspend fun clear() {
        context.registrationDataStore.edit { it.clear() }
    }
}
