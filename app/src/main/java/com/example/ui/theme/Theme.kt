package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF100D02),
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldOnContainer,
    secondary = GoldLight,
    onSecondary = Color(0xFF1B1400),
    secondaryContainer = Color(0xFF2A2416),
    onSecondaryContainer = Color(0xFFFFECC4),
    tertiary = GoldLight,
    onTertiary = Color.Black,
    background = ObsidianBackground,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianSurfaceCard,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF26241D)
)

private val LightColorScheme = lightColorScheme(
    primary = LightGoldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECC4),
    onPrimaryContainer = Color(0xFF2D2000),
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFF7EAC7),
    onSecondaryContainer = Color(0xFF281D00),
    tertiary = GoldDark,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCDC5B2),
    outlineVariant = Color(0xFFE5DFC9)
)

@Composable
fun CnvQrTheme(
    darkTheme: Boolean = true, // Default to luxurious dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
