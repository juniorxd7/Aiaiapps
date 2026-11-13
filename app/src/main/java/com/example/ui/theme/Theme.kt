package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = OnCoralPrimary,
    primaryContainer = CoralContainer,
    onPrimaryContainer = OnCoralContainer,
    secondary = EmeraldSecondary,
    onSecondary = OnEmeraldSecondary,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = OnEmeraldContainer,
    tertiary = AmberTertiary,
    onTertiary = OnAmberTertiary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = CreamBackground,
    onBackground = InkOnBackground,
    surface = PaperSurface,
    onSurface = InkOnBackground,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = OnCoralPrimaryDark,
    primaryContainer = CoralContainerDark,
    onPrimaryContainer = OnCoralContainerDark,
    secondary = EmeraldSecondaryDark,
    onSecondary = OnEmeraldSecondaryDark,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = OnEmeraldContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = OnAmberTertiaryDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = OnAmberContainerDark,
    background = EspressoBackgroundDark,
    onBackground = CreamInkDark,
    surface = EspressoSurfaceDark,
    onSurface = CreamInkDark,
    surfaceVariant = EspressoSurfaceVariantDark,
    onSurfaceVariant = MutedCreamDark,
    outline = EspressoOutlineDark,
    outlineVariant = EspressoOutlineVariantDark,
)

val SorteiaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun SorteiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SorteiaTypography,
        shapes = SorteiaShapes,
        content = content,
    )
}
