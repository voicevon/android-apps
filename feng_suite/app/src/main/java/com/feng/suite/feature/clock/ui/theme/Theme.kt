package com.feng.suite.feature.clock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FossifyOrangePrimary,
    onPrimary = FossifyOrangeOnPrimary,
    primaryContainer = FossifyOrangeContainer,
    onPrimaryContainer = FossifyOrangeOnContainer,
    secondary = FossifyOrangePrimary,
    onSecondary = Color.Black,
    background = FossifyDarkBackground,
    onBackground = FossifyDarkTextPrimary,
    surface = FossifyDarkSurface,
    onSurface = FossifyDarkTextPrimary,
    surfaceVariant = FossifyDarkSurfaceVariant,
    onSurfaceVariant = FossifyDarkTextSecondary,
    outline = FossifyDarkDivider
)

private val LightColorScheme = lightColorScheme(
    primary = FossifyOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = FossifyOrangeOnContainer,
    onPrimaryContainer = FossifyOrangeContainer,
    secondary = FossifyOrangePrimary,
    onSecondary = Color.White,
    background = FossifyLightBackground,
    onBackground = FossifyLightTextPrimary,
    surface = FossifyLightSurface,
    onSurface = FossifyLightTextPrimary,
    surfaceVariant = FossifyLightSurfaceVariant,
    onSurfaceVariant = FossifyLightTextSecondary,
    outline = Color(0xFFDDDDDD)
)

@Composable
fun FossifyClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
