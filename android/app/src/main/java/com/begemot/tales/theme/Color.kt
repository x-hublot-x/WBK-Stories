package com.begemot.tales.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Onyx Canvas (Чистый нейтральный тёмный оникс без синевы)
val OnyxBackground = Color(0xFF08080A)
val OnyxSurface = Color(0xFF101012)
val OnyxSurfaceVariant = Color(0xFF161619)
val OnyxCardBorder = Color(0xFF222225)

// Liquid Glass Tokens
val GlassWhite = Color(0x0DFFFFFF)
val GlassWhiteHighlight = Color(0x1AFFFFFF)
val GlassWhitePressed = Color(0x2EFFFFFF)

val GlassStroke = Brush.verticalGradient(
    listOf(
        Color(0x3AFFFFFF),
        Color(0x08FFFFFF)
    )
)

val GlassStrokeActive = Brush.verticalGradient(
    listOf(
        Color(0x90FFFFFF),
        Color(0x20FFFFFF)
    )
)

// Accent & Text Colors (Нейтральные белые и графитовые оттенки)
val PureWhite = Color(0xFFFFFFFF)
val SoftWhite = Color(0xFFEDEDED)
val MutedSilver = Color(0xFFA0A0A5)
val SubtleGrey = Color(0xFF5A5A60)

// Haptic & Glow Accents
val GlowWhite = Color(0x35FFFFFF)
val AmbientGlow = Color(0x15FFFFFF)

// Frosted Matte Liquid Glass Tokens (Матовый нейтральный оникс)
val FrostedMatteBackground = Brush.verticalGradient(
    listOf(
        Color(0xEE161618), // 93% frosted translucent obsidian glass
        Color(0xF70A0A0C)  // 97% deep matte base
    )
)

val FrostedMatteSurface = Brush.verticalGradient(
    listOf(
        Color(0xDD1C1C20),
        Color(0xEB0E0E10)
    )
)

val FrostedGlassChipActive = Brush.verticalGradient(
    listOf(
        Color(0x45FFFFFF),
        Color(0x20FFFFFF)
    )
)

val FrostedGlassChipInactive = Brush.verticalGradient(
    listOf(
        Color(0x18FFFFFF),
        Color(0x0AFFFFFF)
    )
)

val FrostedGlassStroke = Brush.verticalGradient(
    listOf(
        Color(0x75FFFFFF),
        Color(0x22FFFFFF),
        Color(0x08FFFFFF)
    )
)

val FrostedGlassStrokeActive = Brush.verticalGradient(
    listOf(
        Color(0xB5FFFFFF),
        Color(0x40FFFFFF),
        Color(0x15FFFFFF)
    )
)
