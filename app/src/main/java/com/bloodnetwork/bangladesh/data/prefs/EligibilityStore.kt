package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.bloodnetwork.bangladesh.data.model.EligibilityResultDto

private val Context.eligibilityDataStore by preferencesDataStore(name = "eligibility")

@Serializable
data class EligibilityBundle(
    val answers: Map<String, String> = emptyMap(),
    val result: EligibilityResultDto? = null,
    val lastCheckedAnswers: Map<String, String>? = null
)

class EligibilityStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        // New per-user storage: single JSON map userId -> bundle
        val PER_USER_BUNDLES = stringPreferencesKey("per_user_bundles")
        // Legacy single-user keys (migrated then removed)
        val LEGACY_ANSWERS = stringPreferencesKey("answers")
        val LEGACY_RESULT = stringPreferencesKey("result")
        val LEGACY_LAST_CHECKED = stringPreferencesKey("lastCheckedAnswers")
        val LEGACY_OWNER = stringPreferencesKey("owner_user_id")
    }

    private fun keyFor(userId: String?): String = userId ?: "guest"

    // For UI that still uses Flows, expose a flow that emits the whole per-user map.
    val perUserBundles: Flow<Map<String, EligibilityBundle>> = context.eligibilityDataStore.data.map { prefs ->
        prefs[Keys.PER_USER_BUNDLES]?.let { raw ->
            runCatching { json.decodeFromString<Map<String, EligibilityBundle>>(raw) }.getOrNull()
        } ?: emptyMap()
    }

    // Legacy flows kept for migration only — new code should use per-user suspend getters.
    val answers: Flow<Map<String, String>> = perUserBundles.map { it[keyFor(null)]?.answers ?: emptyMap() }
    val result: Flow<EligibilityResultDto?> = perUserBundles.map { it[keyFor(null)]?.result }
    val lastCheckedAnswers: Flow<Map<String, String>?> = perUserBundles.map { it[keyFor(null)]?.lastCheckedAnswers }
    val ownerUserId: Flow<String?> = context.eligibilityDataStore.data.map { it[Keys.LEGACY_OWNER] }

    private suspend fun readBundles(): MutableMap<String, EligibilityBundle> {
        val raw = context.eligibilityDataStore.data.first()[Keys.PER_USER_BUNDLES]
        return if (raw != null) {
            runCatching { json.decodeFromString<MutableMap<String, EligibilityBundle>>(raw) }.getOrNull() ?: mutableMapOf()
        } else mutableMapOf()
    }

    private suspend fun writeBundles(bundles: Map<String, EligibilityBundle>) {
        context.eligibilityDataStore.edit { prefs ->
            prefs[Keys.PER_USER_BUNDLES] = json.encodeToString(bundles)
        }
    }

    suspend fun getBundle(userId: String?): EligibilityBundle {
        // Migrate legacy single-user data once if per-user empty but legacy keys exist
        val bundles = readBundles()
        if (bundles.isEmpty()) {
            val prefs = context.eligibilityDataStore.data.first()
            val legacyAnswersRaw = prefs[Keys.LEGACY_ANSWERS]
            val legacyResultRaw = prefs[Keys.LEGACY_RESULT]
            val legacyLastRaw = prefs[Keys.LEGACY_LAST_CHECKED]
            val legacyOwner = prefs[Keys.LEGACY_OWNER]
            if (legacyAnswersRaw != null || legacyResultRaw != null) {
                val legacyAnswers = legacyAnswersRaw?.let { runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull() } ?: emptyMap()
                val legacyResult = legacyResultRaw?.let { runCatching { json.decodeFromString<EligibilityResultDto>(it) }.getOrNull() }
                val legacyLast = legacyLastRaw?.let { runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull() }
                val ownerKey = legacyOwner ?: keyFor(userId)
                // If legacy owner matches current user (or no owner), migrate; otherwise keep for old owner
                val targetKey = if (legacyOwner != null) legacyOwner else keyFor(userId)
                bundles[targetKey] = EligibilityBundle(answers = legacyAnswers, result = legacyResult, lastCheckedAnswers = legacyLast)
                writeBundles(bundles)
                // remove legacy keys
                context.eligibilityDataStore.edit { prefs ->
                    prefs.remove(Keys.LEGACY_ANSWERS)
                    prefs.remove(Keys.LEGACY_RESULT)
                    prefs.remove(Keys.LEGACY_LAST_CHECKED)
                    prefs.remove(Keys.LEGACY_OWNER)
                }
                return bundles[targetKey] ?: EligibilityBundle()
            }
        }
        return bundles[keyFor(userId)] ?: EligibilityBundle()
    }

    suspend fun saveAnswers(userId: String?, answers: Map<String, String>) {
        val bundles = readBundles()
        val key = keyFor(userId)
        val existing = bundles[key] ?: EligibilityBundle()
        bundles[key] = existing.copy(answers = answers)
        writeBundles(bundles)
    }

    suspend fun saveAnswersWithOwner(answers: Map<String, String>, ownerUserId: String?) {
        // compat for old call site
        saveAnswers(ownerUserId, answers)
    }

    // compat overload
    suspend fun saveAnswers(answers: Map<String, String>, ownerUserId: String? = null) {
        saveAnswers(ownerUserId, answers)
    }

    suspend fun saveResult(userId: String?, result: EligibilityResultDto) {
        val bundles = readBundles()
        val key = keyFor(userId)
        val existing = bundles[key] ?: EligibilityBundle()
        bundles[key] = existing.copy(result = result)
        writeBundles(bundles)
    }

    suspend fun saveResult(result: EligibilityResultDto, ownerUserId: String? = null) {
        saveResult(ownerUserId, result)
    }

    suspend fun saveLastCheckedAnswers(userId: String?, answers: Map<String, String>) {
        val bundles = readBundles()
        val key = keyFor(userId)
        val existing = bundles[key] ?: EligibilityBundle()
        bundles[key] = existing.copy(lastCheckedAnswers = answers)
        writeBundles(bundles)
    }

    suspend fun saveLastCheckedAnswers(answers: Map<String, String>, ownerUserId: String? = null) {
        saveLastCheckedAnswers(ownerUserId, answers)
    }

    suspend fun setOwner(ownerUserId: String?) {
        // no-op for per-user model; owner is the map key itself
    }

    suspend fun clearAnswers() {
        // legacy clear guest only
        clearForUser(null)
    }

    suspend fun clearResult() {
        val bundles = readBundles()
        val key = keyFor(null)
        bundles[key]?.let { bundles[key] = it.copy(result = null) }
        writeBundles(bundles)
    }

    suspend fun clearLastCheckedAnswers() {
        val bundles = readBundles()
        val key = keyFor(null)
        bundles[key]?.let { bundles[key] = it.copy(lastCheckedAnswers = null) }
        writeBundles(bundles)
    }

    suspend fun clearAll() {
        context.eligibilityDataStore.edit { prefs ->
            prefs.remove(Keys.PER_USER_BUNDLES)
            prefs.remove(Keys.LEGACY_ANSWERS)
            prefs.remove(Keys.LEGACY_RESULT)
            prefs.remove(Keys.LEGACY_LAST_CHECKED)
            prefs.remove(Keys.LEGACY_OWNER)
        }
    }

    suspend fun clearForUser(userId: String?) {
        val bundles = readBundles()
        bundles.remove(keyFor(userId))
        writeBundles(bundles)
    }

    // Per-user clears for repository
    suspend fun clearAnswersForUser(userId: String?) {
        val bundles = readBundles()
        val key = keyFor(userId)
        bundles[key]?.let { bundles[key] = it.copy(answers = emptyMap()) }
        writeBundles(bundles)
    }

    suspend fun clearResultForUser(userId: String?) {
        val bundles = readBundles()
        val key = keyFor(userId)
        bundles[key]?.let { bundles[key] = it.copy(result = null) }
        writeBundles(bundles)
    }

    suspend fun clearLastCheckedForUser(userId: String?) {
        val bundles = readBundles()
        val key = keyFor(userId)
        bundles[key]?.let { bundles[key] = it.copy(lastCheckedAnswers = null) }
        writeBundles(bundles)
    }

    suspend fun saveBundle(userId: String?, bundle: EligibilityBundle) {
        val bundles = readBundles()
        bundles[keyFor(userId)] = bundle
        writeBundles(bundles)
    }

    suspend fun saveBundleForCurrentUser(bundle: EligibilityBundle, userId: String?) {
        saveBundle(userId, bundle)
    }
}
