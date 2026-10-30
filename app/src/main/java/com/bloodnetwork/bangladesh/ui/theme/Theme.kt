package com.bloodnetwork.bangladesh.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BloodRed,
    onPrimary = Color.White,
    primaryContainer = BloodPink,
    onPrimaryContainer = DarkRed,
    secondary = GrayMid,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    background = BloodBackground,
    onBackground = GrayDark,
    surface = BloodSurface,
    onSurface = GrayDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = BloodRed,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = BloodRedLight,
    onPrimary = DarkRed,
    primaryContainer = DarkRed,
    onPrimaryContainer = BloodPink,
    secondary = DarkSecondary,
    onSecondary = GrayDark,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground,
    onBackground = GrayLight,
    surface = DarkSurface,
    onSurface = GrayLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = BloodRedLight,
    onError = DarkRed,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

@Composable
fun BloodNetworkTheme(
    themeMode: com.bloodnetwork.bangladesh.data.prefs.ThemeMode? = null,
    darkTheme: Boolean = themeMode?.let { it == com.bloodnetwork.bangladesh.data.prefs.ThemeMode.Dark } ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // The in-app theme toggle can differ from the system theme, and edge-to-edge (enforced
    // from targetSdk 35) lets the app background show behind the system bars. Keep the bar
    // icons readable by matching their tint to the app's own light/dark choice.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
