package com.begemot.tales.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OnyxGlassColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = OnyxBackground,
    primaryContainer = GlassWhiteHighlight,
    onPrimaryContainer = PureWhite,
    secondary = MutedSilver,
    onSecondary = OnyxBackground,
    background = OnyxBackground,
    onBackground = SoftWhite,
    surface = OnyxSurface,
    onSurface = SoftWhite,
    surfaceVariant = OnyxSurfaceVariant,
    onSurfaceVariant = MutedSilver,
    outline = OnyxCardBorder
)

@Composable
fun KotBegemotTalesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OnyxGlassColorScheme,
        typography = Typography,
        content = content
    )
}
