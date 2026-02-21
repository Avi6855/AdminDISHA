package com.avinashpatil.app.admindisha.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BhimBlue,
    secondary = BhimOrange,
    tertiary = BhimDarkOrange, // Added a tertiary color for variety
    background = BhimDarkGrey,
    surface = BhimDarkGrey, // Consider a slightly lighter shade if needed for contrast
    onPrimary = BhimWhite,
    onSecondary = BhimWhite,
    onTertiary = BhimWhite,
    onBackground = BhimLightGrey,
    onSurface = BhimLightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = BhimBlue,
    secondary = BhimOrange,
    tertiary = BhimLightBlue, // Added a tertiary color for variety
    background = BhimLightGrey,
    surface = BhimWhite,
    onPrimary = BhimWhite,
    onSecondary = BhimWhite,
    onTertiary = BhimDarkGrey, // Text on light blue might need to be dark
    onBackground = BhimDarkGrey,
    onSurface = BhimDarkGrey
)

@Composable
fun AdminDISHATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use the custom BHIM theme
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