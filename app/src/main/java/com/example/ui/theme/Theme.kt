package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OpenWebUiColorScheme = darkColorScheme(
    primary = OpenWebUiAccent,
    onPrimary = Color.White,
    primaryContainer = OpenWebUiSurfaceVariant,
    onPrimaryContainer = OpenWebUiTextPrimary,
    secondary = OpenWebUiAccentCyan,
    onSecondary = Color.White,
    background = OpenWebUiBackground,
    onBackground = OpenWebUiTextPrimary,
    surface = OpenWebUiSurface,
    onSurface = OpenWebUiTextPrimary,
    surfaceVariant = OpenWebUiSurfaceVariant,
    onSurfaceVariant = OpenWebUiTextSecondary,
    outline = OpenWebUiBorder,
    error = OpenWebUiError,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OpenWebUiColorScheme,
        typography = Typography,
        content = content
    )
}
