package com.begemot.tales.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect

// 1. Глубокий матовый градиент толщи жидкого стекла Dark Onyx (чистый нейтральный оникс, без синевы)
val LiquidGlassBackgroundBrush = Brush.verticalGradient(
    listOf(
        Color(0xEA161619),
        Color(0xD80D0D0F)
    )
)

// Активный градиент толщи стекла для выделенных элементов (акцент на белом и ониксе)
val LiquidGlassActiveBackgroundBrush = Brush.verticalGradient(
    listOf(
        Color(0xF22E2E35),
        Color(0xE61C1C22)
    )
)

// Градиент для активного белого акцента
val LiquidGlassWhiteAccentBrush = Brush.verticalGradient(
    listOf(
        Color(0x38FFFFFF),
        Color(0x18FFFFFF)
    )
)
val LiquidGlassGreenAccentBrush = LiquidGlassWhiteAccentBrush

// 2. Световая фаска (мягкий деликатный белый блик по контуру, не режущий глаза)
val LiquidGlassBevelStroke = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.05f)
    )
)

// Активная световая фаска для выделенных элементов (яркий чистый белый акцент)
val LiquidGlassActiveBevelStroke = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.85f),
        Color.White.copy(alpha = 0.30f)
    )
)

val LiquidGlassWhiteBevelStroke = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.95f),
        Color.White.copy(alpha = 0.40f)
    )
)
val LiquidGlassGreenBevelStroke = LiquidGlassWhiteBevelStroke

/**
 * Универсальный модификатор Liquid Frosted Glass (Dark Onyx + Pure White):
 * 1. Аппаратный блюр и текстура шума под стеклом (через Haze в чистом ониксе)
 * 2. Глубокий матовый оникс градиент (комфортный для глаз)
 * 3. Деликатная белая световая фаска (блик по контуру)
 * 4. Закругление краев (clip)
 */
fun Modifier.liquidFrostedGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    hazeState: HazeState? = null,
    isActive: Boolean = false,
    isGreenAccent: Boolean = false,
    isWhiteAccent: Boolean = false,
    borderWidth: Dp = 1.2.dp,
    blurRadius: Dp = 32.dp,
    noiseFactor: Float = 0.04f,
    tint: Color = Color(0x66101012),
    backgroundColor: Color = Color(0xD40A0A0C)
): Modifier = this
    .then(
        if (hazeState != null) {
            Modifier.hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = backgroundColor,
                    tints = listOf(HazeTint(tint)),
                    blurRadius = blurRadius,
                    noiseFactor = noiseFactor
                )
            )
        } else Modifier
    )
    .background(
        brush = when {
            isWhiteAccent || isGreenAccent -> LiquidGlassWhiteAccentBrush
            isActive -> LiquidGlassActiveBackgroundBrush
            else -> LiquidGlassBackgroundBrush
        },
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = when {
            isWhiteAccent || isGreenAccent -> LiquidGlassWhiteBevelStroke
            isActive -> LiquidGlassActiveBevelStroke
            else -> LiquidGlassBevelStroke
        },
        shape = shape
    )
    .clip(shape)
