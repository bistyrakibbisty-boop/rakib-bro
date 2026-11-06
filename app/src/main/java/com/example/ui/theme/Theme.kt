package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = AmberPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = MintSecondary,
    onSecondary = Color.Black,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = MintSecondary,
    tertiary = CyanAccent,
    background = ObsidianBackground,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberPrimaryVariant,
    onPrimaryContainer = Color.Black,
    secondary = MintSecondaryContainer,
    onSecondary = Color.White,
    secondaryContainer = MintSecondary,
    onSecondaryContainer = Color.Black,
    tertiary = CyanAccent,
    background = PureLightBackground,
    onBackground = TextPrimaryLight,
    surface = PureLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PureLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = PureLightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep consistent high-contrast calculator branding while allowing dynamic color if desired
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
