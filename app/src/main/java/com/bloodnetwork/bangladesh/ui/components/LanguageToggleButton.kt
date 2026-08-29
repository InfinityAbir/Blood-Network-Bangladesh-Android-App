package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodnetwork.bangladesh.data.prefs.AppLanguage
import com.bloodnetwork.bangladesh.ui.LocalLanguageStore

/** Mirrors ThemeToggleButton — a single tap flips English/Bangla app-wide and
 * remembers the choice (see LanguageStore). Shows the language you'd switch TO,
 * same convention as the theme icon (which shows the mode you'd switch to). */
@Composable
fun LanguageToggleButton(modifier: Modifier = Modifier) {
    val languageStore = LocalLanguageStore.current ?: return
    val language by languageStore.language.collectAsStateWithLifecycle()

    Text(
        text = if (language == AppLanguage.Bangla) "EN" else "বাং",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clickable { languageStore.toggle() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
    )
}
