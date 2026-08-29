package com.bloodnetwork.bangladesh.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.bloodnetwork.bangladesh.data.prefs.LanguageStore
import com.bloodnetwork.bangladesh.data.prefs.ThemeStore

val LocalVmFactory = staticCompositionLocalOf<VmFactory?> { null }

val LocalThemeStore = staticCompositionLocalOf<ThemeStore?> { null }

val LocalLanguageStore = staticCompositionLocalOf<LanguageStore?> { null }
