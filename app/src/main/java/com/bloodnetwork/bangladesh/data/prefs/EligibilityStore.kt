package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val Context.eligibilityDataStore by preferencesDataStore(name = "eligibility")

class EligibilityStore(private val context: Context) {

    private object Keys {
        val ANSWERS = stringPreferencesKey("answers")
    }

    val answers: Flow<Map<String, String>> = context.eligibilityDataStore.data.map { prefs ->
        prefs[Keys.ANSWERS]?.let { json -> Json.decodeFromString<Map<String, String>>(json) } ?: emptyMap()
    }

    suspend fun saveAnswers(answers: Map<String, String>) {
        context.eligibilityDataStore.edit { prefs ->
            prefs[Keys.ANSWERS] = Json.encodeToString(answers)
        }
    }

    suspend fun clearAnswers() {
        context.eligibilityDataStore.edit { it.remove(Keys.ANSWERS) }
    }
}
