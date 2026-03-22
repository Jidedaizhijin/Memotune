package com.jidedaizhijin.myapplication.ui.theme

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

private val AccentBlue = Color(0xFF8EC5FF)

private val PulseDarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,

    background = Color(0xFF0B0C10),
    surface = Color(0xFF111318),
    surfaceVariant = Color(0xFF1E2230),

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFD5D9E0)
)

private val PulseLightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,

    background = Color(0xFFF8FBFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAF2FF),

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Color(0xFF111318),
    onSurface = Color(0xFF111318),
    onSurfaceVariant = Color(0xFF5D6673)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> PulseDarkColorScheme
        else -> PulseLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}