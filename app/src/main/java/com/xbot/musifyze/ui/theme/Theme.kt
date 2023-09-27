package com.xbot.musifyze.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColors = darkColors(
    primary = Purple80,
    secondary = PurpleGrey80,
    background = Black,
    surface = Black
)

private val LightColors = lightColors(
    primary = Purple40,
    secondary = PurpleGrey40,
    background = White,
    surface = White
)

@Composable
fun MusifyzeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalElevationOverlay provides null) {
        MaterialTheme(
            colors = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}