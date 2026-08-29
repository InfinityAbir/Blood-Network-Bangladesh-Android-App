package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodnetwork.bangladesh.data.prefs.ThemeMode
import com.bloodnetwork.bangladesh.ui.LocalThemeStore
import com.bloodnetwork.bangladesh.ui.i18n.tr

/** Mirrors the website's header theme toggle — a single icon button that flips
 * light/dark and remembers the choice (see ThemeStore). */
@Composable
fun ThemeToggleButton(modifier: Modifier = Modifier) {
    val themeStore = LocalThemeStore.current ?: return
    val mode by themeStore.mode.collectAsStateWithLifecycle()

    IconButton(onClick = { themeStore.toggle() }, modifier = modifier) {
        Icon(
            imageVector = if (mode == ThemeMode.Dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (mode == ThemeMode.Dark) tr("Switch to light mode", "লাইট মোডে যান") else tr("Switch to dark mode", "ডার্ক মোডে যান"),
        )
    }
}
