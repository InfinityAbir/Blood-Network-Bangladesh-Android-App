package com.bloodnetwork.bangladesh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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
    background = BloodBackground,
    onBackground = GrayDark,
    surface = BloodSurface,
    onSurface = GrayDark,
)

private val DarkColors = darkColorScheme(
    primary = BloodRedLight,
    onPrimary = DarkRed,
    primaryContainer = DarkRed,
    onPrimaryContainer = BloodPink,
    secondary = GrayMid,
    onSecondary = Color.White,
)

@Composable
fun BloodNetworkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
