package com.rfacto.shipping.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RFactoPrimary,
    secondary = RFactoTertiary,
    tertiary = RFactoSecondary,
    background = SlateDarkBg,
    surface = CardDarkBg,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF001B3D),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE1E2EC),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE1E2EC),
    surfaceVariant = ColorBorderDark,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF8E95A5)
)

private val LightColorScheme = lightColorScheme(
    primary = RFactoPrimary,
    secondary = RFactoSecondary,
    tertiary = RFactoTertiary,
    background = SlateLightBg,
    surface = CardLightBg,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1A1C1E),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1C1E),
    surfaceVariant = ColorBorder,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF565F71)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve our custom brand colors
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
