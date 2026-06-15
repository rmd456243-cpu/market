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
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    secondary = CobaltSecondary,
    onSecondary = Color.White,
    tertiary = OrangeNagad,
    onTertiary = Color.Black,
    background = DeepSlateBg,
    onBackground = LightText,
    surface = CardSlate,
    onSurface = LightText,
    surfaceVariant = BorderSlate,
    onSurfaceVariant = DimText
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008E3A),
    onPrimary = Color.White,
    secondary = Color(0xFF1565C0),
    onSecondary = Color.White,
    tertiary = Color(0xFFE65100),
    onTertiary = Color.White,
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF1B1F23),
    surface = Color.White,
    onSurface = Color(0xFF1B1F23),
    surfaceVariant = Color(0xFFE1E4E8),
    onSurfaceVariant = Color(0xFF586069)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable default dynamic coloring to preserve our distinct TrustGig theme brand
    content: @Composable () -> Unit,
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
