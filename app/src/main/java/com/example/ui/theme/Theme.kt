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
    primary = TealAccent,
    onPrimary = Color(0xFF003830),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = Color(0xFF70F5DC),

    secondary = AmberAccent,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = Color(0xFFFFDDB3),

    tertiary = IndigoAccent,
    onTertiary = Color(0xFF1E1E66),
    tertiaryContainer = IndigoContainerDark,
    onTertiaryContainer = Color(0xFFD4D6FF),

    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Slate700,

    error = CoralRed,
    onError = Color.White,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = Color(0xFF00201B),

    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = Color(0xFF2E1500),

    tertiary = Color(0xFF4F46E5),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E7FF),
    onTertiaryContainer = Color(0xFF0D0B4A),

    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFFEDEC),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set default to false to showcase intentional FinTech styling
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
