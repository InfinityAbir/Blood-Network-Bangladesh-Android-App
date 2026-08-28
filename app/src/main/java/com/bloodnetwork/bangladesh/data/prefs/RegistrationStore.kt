package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val Context.registrationDataStore by preferencesDataStore(name = "registration")

class RegistrationStore(private val context: Context) {

    private object Keys {
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val PASSWORD = stringPreferencesKey("password")
    }

    data class RegistrationData(
        val firstName: String = "",
        val lastName: String = "",
        val phoneNumber: String = "",
        val password: String = "",
    )

    val data: Flow<RegistrationData> = context.registrationDataStore.data.map { prefs ->
        RegistrationData(
            firstName = prefs[Keys.FIRST_NAME] ?: "",
            lastName = prefs[Keys.LAST_NAME] ?: "",
            phoneNumber = prefs[Keys.PHONE_NUMBER] ?: "",
            password = prefs[Keys.PASSWORD] ?: "",
        )
    }

    suspend fun save(data: RegistrationData) {
        context.registrationDataStore.edit { prefs ->
            prefs[Keys.FIRST_NAME] = data.firstName
            prefs[Keys.LAST_NAME] = data.lastName
            prefs[Keys.PHONE_NUMBER] = data.phoneNumber
            prefs[Keys.PASSWORD] = data.password
        }
    }

    suspend fun clear() {
        context.registrationDataStore.edit { it.clear() }
    }
}
