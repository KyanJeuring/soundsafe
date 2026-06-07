package com.example.soundsafe.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAccentColor = staticCompositionLocalOf<AccentColor> {
    error("No AccentColor provided")
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), // Majestic Purple Light
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    outline = md_theme_light_outline,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = Color(0xFFD0BCFF),
    surfaceTint = Color(0xFF6750A4),
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF), // Majestic Purple Dark
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    outline = md_theme_dark_outline,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = Color(0xFF6750A4),
    surfaceTint = Color(0xFFD0BCFF),
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun SoundSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AccentColor = accents.first(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> {
            val isLightAccent = accentColor.name == "Sunflower Yellow"
            DarkColorScheme.copy(
                primary = accentColor.darkPrimary,
                onPrimary = if (isLightAccent) Color(0xFF212121) else Color.Black,
                primaryContainer = accentColor.darkPrimary.copy(alpha = 0.15f),
                onPrimaryContainer = accentColor.darkPrimary,
                surfaceTint = accentColor.darkPrimary,
                inversePrimary = accentColor.lightPrimary
            )
        }
        else -> LightColorScheme.copy(
            primary = accentColor.lightPrimary,
            onPrimary = Color.White,
            primaryContainer = accentColor.lightPrimary.copy(alpha = 0.08f),
            onPrimaryContainer = accentColor.lightPrimary,
            surfaceTint = accentColor.lightPrimary,
            inversePrimary = accentColor.darkPrimary
        )
    }

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
