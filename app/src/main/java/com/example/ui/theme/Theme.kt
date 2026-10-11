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
    primary = KelontongGreenLight,
    onPrimary = Slate900,
    primaryContainer = KelontongGreenDark,
    onPrimaryContainer = KelontongGreenContainer,
    secondary = KelontongAmber,
    onSecondary = Slate900,
    secondaryContainer = KelontongOnAmberContainer,
    onSecondaryContainer = KelontongAmberContainer,
    tertiary = KelontongGreenLight,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onBackground = Slate100,
    onSurface = Slate100,
    error = StockRed
)

private val LightColorScheme = lightColorScheme(
    primary = KelontongGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KelontongGreenContainer,
    onPrimaryContainer = KelontongOnGreenContainer,
    secondary = KelontongAmber,
    onSecondary = Color.White,
    secondaryContainer = KelontongAmberContainer,
    onSecondaryContainer = KelontongOnAmberContainer,
    tertiary = KelontongGreenLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Slate900,
    onSurface = Slate800,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = StockRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinctive brand emerald green theme
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
