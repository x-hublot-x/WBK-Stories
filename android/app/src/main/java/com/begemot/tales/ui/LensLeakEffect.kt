package com.begemot.tales.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.begemot.tales.theme.PureWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class PerimeterPoint(
    val x: Float,
    val y: Float,
    val tangentRad: Float
)

/**
 * Точный расчет положения точки вдоль периметра скругленного прямоугольника
 */
fun calculatePerimeterPoint(
    fraction: Float,
    w: Float,
    h: Float,
    r: Float
): PerimeterPoint {
    val safeR = r.coerceAtMost(minOf(w, h) / 2f).coerceAtLeast(0.1f)
    val l1 = (w - 2f * safeR).coerceAtLeast(0f)
    val l2 = (PI.toFloat() * safeR / 2f)
    val l3 = (h - 2f * safeR).coerceAtLeast(0f)
    val l4 = l2
    val l5 = l1
    val l6 = l2
    val l7 = l3
    val l8 = l2

    val totalLength = l1 + l2 + l3 + l4 + l5 + l6 + l7 + l8
    if (totalLength <= 0f) return PerimeterPoint(0f, 0f, 0f)

    var d = (fraction % 1f) * totalLength
    if (d < 0f) d += totalLength

    // 1. Верхняя прямая (слева направо)
    if (d <= l1) {
        return PerimeterPoint(safeR + d, 0f, 0f)
    }
    d -= l1

    // 2. Верхний правый угол
    if (d <= l2) {
        val angle = -PI.toFloat() / 2f + (d / l2) * (PI.toFloat() / 2f)
        val cx = w - safeR
        val cy = safeR
        return PerimeterPoint(cx + safeR * cos(angle), cy + safeR * sin(angle), angle + PI.toFloat() / 2f)
    }
    d -= l2

    // 3. Правая прямая (сверху вниз)
    if (d <= l3) {
        return PerimeterPoint(w, safeR + d, PI.toFloat() / 2f)
    }
    d -= l3

    // 4. Нижний правый угол
    if (d <= l4) {
        val angle = 0f + (d / l4) * (PI.toFloat() / 2f)
        val cx = w - safeR
        val cy = h - safeR
        return PerimeterPoint(cx + safeR * cos(angle), cy + safeR * sin(angle), angle + PI.toFloat() / 2f)
    }
    d -= l4

    // 5. Нижняя прямая (справа налево)
    if (d <= l5) {
        return PerimeterPoint(w - safeR - d, h, PI.toFloat())
    }
    d -= l5

    // 6. Нижний левый угол
    if (d <= l6) {
        val angle = PI.toFloat() / 2f + (d / l6) * (PI.toFloat() / 2f)
        val cx = safeR
        val cy = h - safeR
        return PerimeterPoint(cx + safeR * cos(angle), cy + safeR * sin(angle), angle + PI.toFloat() / 2f)
    }
    d -= l6

    // 7. Левая прямая (снизу вверх)
    if (d <= l7) {
        return PerimeterPoint(0f, h - safeR - d, 3f * PI.toFloat() / 2f)
    }
    d -= l7

    // 8. Верхний левый угол
    val angle = PI.toFloat() + (d / l8.coerceAtLeast(0.001f)) * (PI.toFloat() / 2f)
    val cx = safeR
    val cy = safeR
    return PerimeterPoint(cx + safeR * cos(angle), cy + safeR * sin(angle), angle + PI.toFloat() / 2f)
}

private val FlareZeroAlpha = Color(0x00FFFFFF)

/**
 * Отрисовка мягких оптических бликов света СТРОГО ПОЗАДИ обложки:
 * - Радиус засветов увеличен в 3 раза для создания масштабной кино-ауры
 * - Центральные точки смещены внутрь под обложку, а градиент сделан ультра-мягким,
 *   благодаря чему яркие точки полностью скрыты (нет слепящих хотспотов)
 * - 6 независимых источников света с медленным хаотичным дыханием размеров
 * - Без паразитного вращения на углах
 */
fun DrawScope.drawLensLeaksBehind(
    time: Float,
    alpha: Float,
    cardW: Float,
    cardH: Float,
    offsetX: Float,
    offsetY: Float,
    cornerRadiusPx: Float
) {
    if (alpha <= 0.005f) return
    val minDim = minOf(cardW, cardH)
    if (minDim <= 0f) return

    // Смещение центра бликов внутрь под непрозрачную обложку, чтобы яркие центральные точки никогда не были видны
    val insetDist = minDim * 0.16f

    // 1. Главный атмосферный белый ореол (чуть быстрее, мягкая яркость)
    val frac1 = (0.02f + 0.015f * time + 0.040f * sin(0.092f * time) + 0.025f * cos(0.160f * time)).mod(1f)
    val sizeFactor1 = (0.65f + 0.35f * sin(0.085f * time) + 0.20f * cos(0.150f * time)).coerceIn(0.45f, 1.25f)
    val r1 = (minDim * 0.96f) * sizeFactor1
    val pt1 = calculatePerimeterPoint(frac1, cardW, cardH, cornerRadiusPx)
    val normAngle1 = pt1.tangentRad + (PI.toFloat() / 2f)
    val cx1 = offsetX + pt1.x + insetDist * cos(normAngle1)
    val cy1 = offsetY + pt1.y + insetDist * sin(normAngle1)

    drawSingleSoftFlare(
        center = Offset(cx1, cy1),
        radius = r1,
        alpha = alpha * (0.75f + 0.25f * cos(0.12f * time)) * 0.75f
    )

    // 2. Вторичный крупный встречный ореол
    val frac2 = (0.52f - 0.0135f * time + 0.042f * cos(0.080f * time) - 0.028f * sin(0.145f * time)).mod(1f)
    val sizeFactor2 = (0.62f + 0.32f * cos(0.075f * time + 1.2f) - 0.18f * sin(0.135f * time)).coerceIn(0.42f, 1.20f)
    val r2 = (minDim * 0.84f) * sizeFactor2
    val pt2 = calculatePerimeterPoint(frac2, cardW, cardH, cornerRadiusPx)
    val normAngle2 = pt2.tangentRad + (PI.toFloat() / 2f)
    val cx2 = offsetX + pt2.x + insetDist * cos(normAngle2)
    val cy2 = offsetY + pt2.y + insetDist * sin(normAngle2)

    drawSingleSoftFlare(
        center = Offset(cx2, cy2),
        radius = r2,
        alpha = alpha * (0.72f + 0.28f * sin(0.11f * time + 1.0f)) * 0.70f
    )

    // 3. Правый боковой плавающий ореол
    val frac3 = (0.26f + 0.0120f * time + 0.035f * sin(0.105f * time) + 0.022f * cos(0.175f * time)).mod(1f)
    val sizeFactor3 = (0.60f + 0.30f * sin(0.095f * time + 2.5f) + 0.22f * cos(0.165f * time)).coerceIn(0.40f, 1.18f)
    val r3 = (minDim * 0.72f) * sizeFactor3
    val pt3 = calculatePerimeterPoint(frac3, cardW, cardH, cornerRadiusPx)
    val normAngle3 = pt3.tangentRad + (PI.toFloat() / 2f)
    val cx3 = offsetX + pt3.x + insetDist * cos(normAngle3)
    val cy3 = offsetY + pt3.y + insetDist * sin(normAngle3)

    drawSingleSoftFlare(
        center = Offset(cx3, cy3),
        radius = r3,
        alpha = alpha * (0.68f + 0.32f * cos(0.13f * time + 2.2f)) * 0.65f
    )

    // 4. Левый боковой встречный ореол
    val frac4 = (0.76f - 0.0125f * time + 0.038f * cos(0.072f * time) - 0.024f * sin(0.138f * time)).mod(1f)
    val sizeFactor4 = (0.58f + 0.32f * cos(0.082f * time + 0.7f) + 0.18f * sin(0.155f * time)).coerceIn(0.40f, 1.15f)
    val r4 = (minDim * 0.75f) * sizeFactor4
    val pt4 = calculatePerimeterPoint(frac4, cardW, cardH, cornerRadiusPx)
    val normAngle4 = pt4.tangentRad + (PI.toFloat() / 2f)
    val cx4 = offsetX + pt4.x + insetDist * cos(normAngle4)
    val cy4 = offsetY + pt4.y + insetDist * sin(normAngle4)

    drawSingleSoftFlare(
        center = Offset(cx4, cy4),
        radius = r4,
        alpha = alpha * (0.70f + 0.30f * sin(0.12f * time + 3.0f)) * 0.68f
    )

    // 5. Верхне-правый блуждающий ореол
    val frac5 = (0.14f + 0.0105f * time + 0.045f * sin(0.120f * time) - 0.026f * cos(0.210f * time)).mod(1f)
    val sizeFactor5 = (0.55f + 0.35f * sin(0.110f * time + 1.8f) - 0.20f * cos(0.190f * time)).coerceIn(0.38f, 1.15f)
    val r5 = (minDim * 0.63f) * sizeFactor5
    val pt5 = calculatePerimeterPoint(frac5, cardW, cardH, cornerRadiusPx)
    val normAngle5 = pt5.tangentRad + (PI.toFloat() / 2f)
    val cx5 = offsetX + pt5.x + insetDist * cos(normAngle5)
    val cy5 = offsetY + pt5.y + insetDist * sin(normAngle5)

    drawSingleSoftFlare(
        center = Offset(cx5, cy5),
        radius = r5,
        alpha = alpha * (0.65f + 0.35f * sin(0.15f * time + 1.5f)) * 0.58f
    )

    // 6. Нижне-левый световой всполох
    val frac6 = (0.64f - 0.0110f * time - 0.036f * sin(0.095f * time) + 0.028f * cos(0.165f * time)).mod(1f)
    val sizeFactor6 = (0.52f + 0.36f * cos(0.098f * time + 2.8f) + 0.22f * sin(0.180f * time)).coerceIn(0.35f, 1.12f)
    val r6 = (minDim * 0.57f) * sizeFactor6
    val pt6 = calculatePerimeterPoint(frac6, cardW, cardH, cornerRadiusPx)
    val normAngle6 = pt6.tangentRad + (PI.toFloat() / 2f)
    val cx6 = offsetX + pt6.x + insetDist * cos(normAngle6)
    val cy6 = offsetY + pt6.y + insetDist * sin(normAngle6)

    drawSingleSoftFlare(
        center = Offset(cx6, cy6),
        radius = r6,
        alpha = alpha * (0.60f + 0.40f * cos(0.14f * time + 0.5f)) * 0.55f
    )
}

/**
 * Отрисовка отдельного мягкого облака света с широким диффузным затуханием.
 * Никаких ярких центральных точек: пиковая альфа деликатна, а центр спрятан под обложкой.
 */
private fun DrawScope.drawSingleSoftFlare(
    center: Offset,
    radius: Float,
    alpha: Float
) {
    if (radius <= 1f || alpha <= 0.005f) return

    val spotBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to PureWhite.copy(alpha = (alpha * 0.26f).coerceIn(0f, 1f)),
            0.15f to PureWhite.copy(alpha = (alpha * 0.20f).coerceIn(0f, 1f)),
            0.35f to PureWhite.copy(alpha = (alpha * 0.11f).coerceIn(0f, 1f)),
            0.60f to PureWhite.copy(alpha = (alpha * 0.042f).coerceIn(0f, 1f)),
            0.82f to PureWhite.copy(alpha = (alpha * 0.008f).coerceIn(0f, 1f)),
            1.0f to FlareZeroAlpha
        ),
        center = center,
        radius = radius
    )

    drawCircle(
        brush = spotBrush,
        radius = radius,
        center = center
    )
}

/**
 * Контейнер обложки со стильной оптической анимацией белых световых пятен (Lens Leaks):
 * - Световые пятна расположены СТРОГО ПОЗАДИ обложки (картинка остается чистой)
 * - Расширенное поле рисования (overflow), исключающее любые срезы/линии по краям
 * - 6 источников света, хаотично меняющих свой размер и медленно гуляющих по периметру
 * - Полное отсутствие паразитного вращения на углах — мягкое круговое распределение света
 * - Активируется только при воспроизведении (isPlaying == true) с плавным затуханием
 */
@Composable
fun LensLeakCoverBox(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    cornerRadius: Dp = 26.dp,
    overflow: Dp = 160.dp,
    intensity: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val masterAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "lensLeakAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "lensLeakTransition")
    val rawTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000_000, easing = LinearEasing)
        ),
        label = "lensLeakTime"
    )

    val overflowPx = with(LocalDensity.current) { overflow.roundToPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Световые пятна СТРОГО ПОЗАДИ обложки с запасом (overflow), чтобы свет никогда не обрезался
        if (masterAlpha > 0.005f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .layout { measurable, constraints ->
                        val targetW = if (constraints.hasBoundedWidth) constraints.maxWidth + overflowPx * 2 else constraints.minWidth + overflowPx * 2
                        val targetH = if (constraints.hasBoundedHeight) constraints.maxHeight + overflowPx * 2 else constraints.minHeight + overflowPx * 2
                        val placeable = measurable.measure(Constraints.fixed(targetW, targetH))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(-overflowPx, -overflowPx)
                        }
                    }
            ) {
                val cardW = size.width - overflowPx * 2
                val cardH = size.height - overflowPx * 2
                if (cardW > 0f && cardH > 0f) {
                    drawLensLeaksBehind(
                        time = rawTime,
                        alpha = masterAlpha * intensity,
                        cardW = cardW,
                        cardH = cardH,
                        offsetX = overflowPx.toFloat(),
                        offsetY = overflowPx.toFloat(),
                        cornerRadiusPx = cornerRadius.toPx()
                    )
                }
            }
        }

        // 2. Сама обложка рисуется ПОВЕРХ засветов (засветы не попадают на картинку)
        content()
    }
}
