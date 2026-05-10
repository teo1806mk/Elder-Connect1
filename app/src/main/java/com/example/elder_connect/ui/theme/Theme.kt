package com.example.elder_connect.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.os.Build

/**
 * Light Color Scheme - τα ζεστά χρώματα του Figma
 */
private val LightColorScheme = lightColorScheme(
    primary = ElderOrange,
    onPrimary = ElderCream,
    primaryContainer = ElderOrangeLight,
    onPrimaryContainer = ElderTextPrimary,

    secondary = ElderOrangeDark,
    onSecondary = ElderCream,
    secondaryContainer = ElderBeige,
    onSecondaryContainer = ElderTextPrimary,

    tertiary = ElderGreen,
    onTertiary = ElderCream,

    background = ElderCream,
    onBackground = ElderTextPrimary,

    surface = ElderCream,
    onSurface = ElderTextPrimary,
    surfaceVariant = ElderBeige,
    onSurfaceVariant = ElderTextSecondary,

    error = ElderRed,
    onError = ElderCream
)

/**
 * Dark Color Scheme
 */
private val DarkColorScheme = darkColorScheme(
    primary = ElderOrangeDarkMode,
    onPrimary = ElderTextPrimary,
    primaryContainer = ElderOrangeDark,
    onPrimaryContainer = ElderCream,

    secondary = ElderOrangeLight,
    onSecondary = ElderTextPrimary,

    tertiary = ElderGreen,

    background = ElderBackgroundDark,
    onBackground = ElderCream,

    surface = ElderSurfaceDark,
    onSurface = ElderCream,

    error = ElderRed,
    onError = ElderCream
)

/**
 * ElderConnectTheme.
 *
 * Υποστηρίζει:
 * - Light/Dark mode (αυτόματη ανίχνευση από συσκευή)
 * - Dynamic Colors (Material You) για Android 12+ -> ικανοποιεί την απαίτηση
 *   "Υποστήριξη δυναμικής αλλαγής χρωμάτων με βάση το material design"
 */
@Composable
fun ElderConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Material You
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Material You (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ElderTypography,
        content = content
    )
}