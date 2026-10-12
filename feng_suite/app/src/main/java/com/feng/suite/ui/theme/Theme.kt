package com.feng.suite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = PrimaryPurple,
    background = BackgroundDark,
    surface = CardBackgroundDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextWhitePrimary,
    onSurface = TextWhitePrimary
)

@Composable
fun FengSuiteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
