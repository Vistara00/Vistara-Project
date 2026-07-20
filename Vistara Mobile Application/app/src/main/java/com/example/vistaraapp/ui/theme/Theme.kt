package com.example.vistaraapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun VistaraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = PrimaryGreen,
            secondary = PrimaryGreen,
            tertiary = PrimaryGreen,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = PureWhite,
            onSecondary = PureWhite,
            onBackground = PureWhite,
            onSurface = PureWhite
        )
    } else {
        lightColorScheme(
            primary = PrimaryGreen,
            secondary = PrimaryGreen,
            tertiary = PrimaryGreen,
            background = PureWhite,
            surface = PureWhite,
            onPrimary = PureWhite,
            onSecondary = PureWhite,
            onBackground = DarkText,
            onSurface = DarkText,
            error = ErrorRed,
            onError = PureWhite
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}