package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore

/**
 * Factory for Keystore-backed [EncryptedSharedPreferences] (AES-256-GCM values, AES-256-SIV keys).
 *
 * Opening an encrypted file throws when its Keystore master key is gone or unusable (device
 * restore, lock-screen reset, some OEM Keystore bugs). Left unhandled that crashes the app on
 * every launch, so recovery is staged: first drop only this file and retry, then, if the
 * master key itself is the problem, drop the key too. Losing the contents only signs the
 * user out / clears a cache, which beats a crash loop.
 */
object SecurePrefs {

    fun create(context: Context, name: String): SharedPreferences {
        val appContext = context.applicationContext
        return try {
            open(appContext, name)
        } catch (_: Exception) {
            runCatching { appContext.deleteSharedPreferences(name) }
            try {
                open(appContext, name)
            } catch (_: Exception) {
                runCatching { appContext.deleteSharedPreferences(name) }
                runCatching {
                    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                        .deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                }
                open(appContext, name)
            }
        }
    }

    private fun open(context: Context, name: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Removes a pre-encryption DataStore file so upgrading installs don't keep a plaintext copy. */
    fun deleteLegacyDataStore(context: Context, name: String) {
        runCatching { java.io.File(context.filesDir, "datastore/$name.preferences_pb").delete() }
    }
}

/**
 * Small observable string map over encrypted preferences. Drop-in for the plaintext
 * DataStore the draft/cache stores used before: reads are a hot [StateFlow] snapshot,
 * writes go through [edit].
 */
class SecureStringStore(context: Context, name: String, legacyDataStoreName: String? = null) {

    private val prefs: SharedPreferences = run {
        legacyDataStoreName?.let { SecurePrefs.deleteLegacyDataStore(context, it) }
        SecurePrefs.create(context, name)
    }

    private val _data = MutableStateFlow(snapshot())
    val data: StateFlow<Map<String, String>> = _data.asStateFlow()

    private fun snapshot(): Map<String, String> =
        prefs.all.mapNotNull { (k, v) -> (v as? String)?.let { k to it } }.toMap()

    @Synchronized
    fun edit(block: (MutableMap<String, String>) -> Unit) {
        val map = snapshot().toMutableMap()
        block(map)
        prefs.edit().clear().apply {
            map.forEach { (k, v) -> putString(k, v) }
        }.apply()
        _data.value = map
    }
}
