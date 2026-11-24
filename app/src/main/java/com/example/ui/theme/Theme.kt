package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TechCyan,
    onPrimary = Color(0xFF002B30),
    secondary = TechLime,
    onSecondary = Color(0xFF003310),
    tertiary = TechAmber,
    onTertiary = Color(0xFF331E00),
    background = DarkSlateBg,
    onBackground = TechTextPrimary,
    surface = DarkSlateSurface,
    onSurface = TechTextPrimary,
    surfaceVariant = DarkSlateSurfaceVariant,
    onSurfaceVariant = TechTextSecondary,
    outline = TechBorder,
    error = TechCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
