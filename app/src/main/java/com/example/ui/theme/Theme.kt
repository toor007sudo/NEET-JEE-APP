package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    tertiary = AccentPurple,
    background = DarkBg,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for neon styling
    content: @Composable () -> Unit
) {
    // Disabled dynamicColor to guarantee pixel-perfect neon styling
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
