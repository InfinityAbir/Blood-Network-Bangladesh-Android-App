package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.bloodnetwork.bangladesh.data.model.EligibilityResultDto

@Serializable
data class EligibilityBundle(
    val answers: Map<String, String> = emptyMap(),
    val result: EligibilityResultDto? = null,
    val lastCheckedAnswers: Map<String, String>? = null
)

/**
 * Per-user cache of the eligibility questionnaire (health answers + last result).
 * Stored in Keystore-encrypted preferences; the plaintext DataStore used by earlier
 * versions is deleted on first open. The server keeps the authoritative copy, so a
 * cache lost to that migration is simply refetched / re-answered.
 */
class EligibilityStore(context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val store = SecureStringStore(context, "eligibility_secure", legacyDataStoreName = "eligibility")

    private object Keys {
        // Single JSON map userId -> bundle
        const val PER_USER_BUNDLES = "per_user_bundles"
        const val LEGACY_OWNER = "owner_user_id"
    }

    private fun keyFor(userId: String?): String = userId ?: "guest"

    private fun decode(raw: String?): MutableMap<String, EligibilityBundle> =
        raw?.let { runCatching { json.decodeFromString<MutableMap<String, EligibilityBundle>>(it) }.getOrNull() }
            ?: mutableMapOf()

    // For UI that still uses Flows, expose a flow that emits the whole per-user map.
    val perUserBundles: Flow<Map<String, EligibilityBundle>> = store.data.map { decode(it[Keys.PER_USER_BUNDLES]) }

    // Legacy flows kept for call-site compatibility — new code should use per-user suspend getters.
    val answers: Flow<Map<String, String>> = perUserBundles.map { it[keyFor(null)]?.answers ?: emptyMap() }
    val result: Flow<EligibilityResultDto?> = perUserBundles.map { it[keyFor(null)]?.result }
    val lastCheckedAnswers: Flow<Map<String, String>?> = perUserBundles.map { it[keyFor(null)]?.lastCheckedAnswers }
    val ownerUserId: Flow<String?> = store.data.map { it[Keys.LEGACY_OWNER] }

    private fun readBundles(): MutableMap<String, EligibilityBundle> =
        decode(store.data.value[Keys.PER_USER_BUNDLES])

    private fun writeBundles(bundles: Map<String, EligibilityBundle>) {
        store.edit { it[Keys.PER_USER_BUNDLES] = json.encodeToString(bundles) }
    }

    private fun update(userId: String?, transform: (EligibilityBundle) -> EligibilityBundle) {
        val bundles = readBundles()
        val key = keyFor(userId)
        bundles[key] = transform(bundles[key] ?: EligibilityBundle())
        writeBundles(bundles)
    }

    suspend fun getBundle(userId: String?): EligibilityBundle = readBundles()[keyFor(userId)] ?: EligibilityBundle()

    suspend fun saveAnswers(userId: String?, answers: Map<String, String>) {
        update(userId) { it.copy(answers = answers) }
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
        update(userId) { it.copy(result = result) }
    }

    suspend fun saveResult(result: EligibilityResultDto, ownerUserId: String? = null) {
        saveResult(ownerUserId, result)
    }

    suspend fun saveLastCheckedAnswers(userId: String?, answers: Map<String, String>) {
        update(userId) { it.copy(lastCheckedAnswers = answers) }
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
        clearResultForUser(null)
    }

    suspend fun clearLastCheckedAnswers() {
        clearLastCheckedForUser(null)
    }

    suspend fun clearAll() {
        store.edit {
            it.remove(Keys.PER_USER_BUNDLES)
            it.remove(Keys.LEGACY_OWNER)
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
