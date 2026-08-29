package com.bloodnetwork.bangladesh.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.bloodnetwork.bangladesh.data.prefs.AppLanguage

/** Current display language for the whole composition tree. Set once at the app
 * root (MainActivity) from LanguageStore; every screen reads it via [tr]. Defaults
 * to English so previews/tests that don't provide it still render correctly. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.English }

/** Runtime (no resource-id, no activity recreate) English/Bangla text switch —
 * chosen over Android string resources because MainActivity is a plain
 * ComponentActivity (no AppCompat locale backport), and this keeps the English
 * source text inline next to its translation for easy review. */
@Composable
@ReadOnlyComposable
fun tr(en: String, bn: String): String = if (LocalAppLanguage.current == AppLanguage.Bangla) bn else en
