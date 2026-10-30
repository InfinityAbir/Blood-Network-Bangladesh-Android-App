package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists in-progress registration fields so the form survives process death.
 * The password is deliberately never stored here — only kept in the screen's
 * in-memory state — so it never lands on disk before the account exists.
 * Name and phone are personal data, so they sit in Keystore-encrypted storage.
 */
class RegistrationStore(context: Context) {

    // Earlier versions kept this in a plaintext DataStore (and, before that, the raw
    // password). The legacy file is deleted when this store is first opened.
    private val store = SecureStringStore(context, "registration_secure", legacyDataStoreName = "registration")

    private object Keys {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val PHONE_NUMBER = "phone_number"
    }

    data class RegistrationData(
        val firstName: String = "",
        val lastName: String = "",
        val phoneNumber: String = "",
    )

    val data: Flow<RegistrationData> = store.data.map { prefs ->
        RegistrationData(
            firstName = prefs[Keys.FIRST_NAME] ?: "",
            lastName = prefs[Keys.LAST_NAME] ?: "",
            phoneNumber = prefs[Keys.PHONE_NUMBER] ?: "",
        )
    }

    suspend fun save(data: RegistrationData) {
        store.edit { prefs ->
            prefs[Keys.FIRST_NAME] = data.firstName
            prefs[Keys.LAST_NAME] = data.lastName
            prefs[Keys.PHONE_NUMBER] = data.phoneNumber
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }
}
