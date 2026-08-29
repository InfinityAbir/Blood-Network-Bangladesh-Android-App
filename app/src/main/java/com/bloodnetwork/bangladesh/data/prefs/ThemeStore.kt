package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { Light, Dark }

/** Persists the user's light/dark theme choice — mirrors the website's ThemeService:
 * a plain two-way toggle that starts from the system preference on first launch, then
 * remembers whatever the user explicitly picks. Plain (unencrypted) prefs — not sensitive
 * data, unlike TokenStore's session tokens. */
class ThemeStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(readInitial())
    val mode: StateFlow<ThemeMode> = _mode

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _mode.value = mode
    }

    fun toggle() {
        setMode(if (_mode.value == ThemeMode.Dark) ThemeMode.Light else ThemeMode.Dark)
    }

    private fun readInitial(): ThemeMode {
        val stored = prefs.getString(KEY_MODE, null)
        if (stored != null) return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.Light)

        val nightMode = appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) ThemeMode.Dark else ThemeMode.Light
    }

    private companion object {
        const val KEY_MODE = "mode"
    }
}
