package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Material 3 Expressive Shapes
val MaterialKitShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val WiseShapes = MaterialKitShapes

// Material 3 Light Color Scheme (Sky Aqua Torque Blue)
val TorqueLightColorScheme = lightColorScheme(
    primary = TorqueBluePrimary,
    onPrimary = TorqueBlueOnPrimary,
    primaryContainer = TorqueBlueContainer,
    onPrimaryContainer = TorqueBlueOnContainer,
    secondary = TorqueBlueSecondary,
    onSecondary = TorqueBlueOnSecondary,
    secondaryContainer = TorqueBlueSecondaryContainer,
    onSecondaryContainer = TorqueBlueOnSecondaryContainer,
    tertiary = TorqueBlueTertiary,
    onTertiary = TorqueBlueOnTertiary,
    tertiaryContainer = TorqueBlueTertiaryContainer,
    onTertiaryContainer = TorqueBlueOnTertiaryContainer,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = TorqueLightSurface,
    onBackground = TorqueLightOnSurface,
    surface = TorqueLightSurface,
    onSurface = TorqueLightOnSurface,
    surfaceVariant = TorqueLightSurfaceVariant,
    onSurfaceVariant = TorqueLightOnSurfaceVariant,
    surfaceContainerLowest = TorqueLightSurfaceContainerLowest,
    surfaceContainerLow = TorqueLightSurfaceContainerLow,
    surfaceContainer = TorqueLightSurfaceContainer,
    surfaceContainerHigh = TorqueLightSurfaceContainerHigh,
    surfaceContainerHighest = TorqueLightSurfaceContainerHighest,
    outline = TorqueLightOutline,
    outlineVariant = TorqueLightOutlineVariant
)

// Dark fallback scheme for dual-mode studio camera viewfinder
val TorqueDarkColorScheme = darkColorScheme(
    primary = ChationYellow,
    onPrimary = ChationDark,
    primaryContainer = ChationDarkSurface,
    onPrimaryContainer = ChationYellow,
    secondary = ChationYellowActive,
    onSecondary = ChationDark,
    secondaryContainer = Color(0xFF242730),
    onSecondaryContainer = Color.White,
    tertiary = ChationLavender,
    onTertiary = ChationOnLavender,
    background = StudioDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = StudioCardBg,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = StudioBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Defaults to Light Theme per user request
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TorqueDarkColorScheme else TorqueLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val statusBarColor = if (darkTheme) StudioDarkBg else TorqueLightSurface
                val navBarColor = if (darkTheme) StudioDarkBg else TorqueLightSurface
                window.statusBarColor = statusBarColor.toArgb()
                window.navigationBarColor = navBarColor.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MaterialKitShapes,
        content = content
    )
}
