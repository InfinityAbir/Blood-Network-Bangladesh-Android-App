package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage { English, Bangla }

/** Persists the user's English/Bangla choice — same shape as ThemeStore: a plain
 * two-way toggle that starts in English on first launch, then remembers whatever
 * the user explicitly picks. Plain (unencrypted) prefs — not sensitive data. */
class LanguageStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(readInitial())
    val language: StateFlow<AppLanguage> = _language

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _language.value = language
    }

    fun toggle() {
        setLanguage(if (_language.value == AppLanguage.Bangla) AppLanguage.English else AppLanguage.Bangla)
    }

    private fun readInitial(): AppLanguage {
        val stored = prefs.getString(KEY_LANGUAGE, null) ?: return AppLanguage.English
        return runCatching { AppLanguage.valueOf(stored) }.getOrDefault(AppLanguage.English)
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
    }
}
