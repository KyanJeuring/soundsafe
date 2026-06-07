package com.example.soundsafe.ui.compose

import androidx.compose.ui.graphics.Color

// Light Theme Colors - Purple Palette
val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)
val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_light_onTertiaryContainer = Color(0xFF31111D)
val md_theme_light_error = Color(0xFFB3261E)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFF9DEDC)
val md_theme_light_onErrorContainer = Color(0xFF410E0B)
val md_theme_light_outline = Color(0xFF79747E)
val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF1C1B1F)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF1C1B1F)
val md_theme_light_surfaceVariant = Color(0xFFF4F0F6)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)
val md_theme_light_inverseOnSurface = Color(0xFFF4EFF4)
val md_theme_light_inverseSurface = Color(0xFF313033)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_outlineVariant = Color(0xFFCAC4D0)
val md_theme_light_scrim = Color(0xFF000000)

// Dark Theme Colors - Vibrant Purple Palette
val md_theme_dark_secondary = Color(0xFFCCC2DC)
val md_theme_dark_onSecondary = Color(0xFF332D41)
val md_theme_dark_secondaryContainer = Color(0xFF4A4458)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8DEF8)
val md_theme_dark_tertiary = Color(0xFFEFB8C8)
val md_theme_dark_onTertiary = Color(0xFF492532)
val md_theme_dark_tertiaryContainer = Color(0xFF633B48)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8E4)
val md_theme_dark_error = Color(0xFFF2B8B5)
val md_theme_dark_onError = Color(0xFF601410)
val md_theme_dark_errorContainer = Color(0xFF8C1D18)
val md_theme_dark_onErrorContainer = Color(0xFFF9DEDC)
val md_theme_dark_outline = Color(0xFF938F99)
val md_theme_dark_background = Color(0xFF1C1B1F) // Deep Purple-Grey
val md_theme_dark_onBackground = Color(0xFFE6E1E5)
val md_theme_dark_surface = Color(0xFF1C1B1F)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
val md_theme_dark_surfaceVariant = Color(0xFF49454F) // Card Background
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D0)
val md_theme_dark_inverseOnSurface = Color(0xFF1C1B1F)
val md_theme_dark_inverseSurface = Color(0xFFE6E1E5)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_outlineVariant = Color(0xFF49454F)
val md_theme_dark_scrim = Color(0xFF000000)

// Accent Colors
data class AccentColor(
    val name: String,
    val lightPrimary: Color,
    val darkPrimary: Color,
    val darkerPrimary: Color // For special styling in Dark Mode
)

val accents = listOf(
    AccentColor("Majestic Purple", Color(0xFF6750A4), Color(0xFFD0BCFF), Color(0xFF4F378B)),
    AccentColor("Ocean Blue", Color(0xFF039BE5), Color(0xFF00B0FF), Color(0xFF01579B)),
    AccentColor("Grasshopper Green", Color(0xFF43A047), Color(0xFF00E676), Color(0xFF1B5E20)),
    AccentColor("Sunflower Yellow", Color(0xFFFBC02D), Color(0xFFFFEA00), Color(0xFF212121)),
    AccentColor("Fox Orange", Color(0xFFF4511E), Color(0xFFFF9100), Color(0xFFBF360C)),
    AccentColor("Ladybug Red", Color(0xFFD32F2F), Color(0xFFFF5252), Color(0xFFB71C1C)),
    AccentColor("Flamingo Pink", Color(0xFFE91E63), Color(0xFFF06292), Color(0xFFAD1457))
)
