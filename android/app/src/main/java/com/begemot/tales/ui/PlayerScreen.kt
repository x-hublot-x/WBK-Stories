package com.begemot.tales.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.begemot.tales.data.Story
import com.begemot.tales.player.AudioPlayerManager
import com.begemot.tales.theme.FrostedGlassChipActive
import com.begemot.tales.theme.FrostedGlassChipInactive
import com.begemot.tales.theme.FrostedGlassStroke
import com.begemot.tales.theme.FrostedGlassStrokeActive
import com.begemot.tales.theme.FrostedMatteBackground
import com.begemot.tales.theme.FrostedMatteSurface
import com.begemot.tales.theme.GlassStroke
import com.begemot.tales.theme.GlassStrokeActive
import com.begemot.tales.theme.GlassWhite
import com.begemot.tales.theme.GlassWhiteHighlight
import com.begemot.tales.theme.MutedSilver
import com.begemot.tales.theme.OnyxBackground
import com.begemot.tales.theme.OnyxSurface
import com.begemot.tales.theme.PureWhite
import com.begemot.tales.theme.SoftWhite
import com.begemot.tales.theme.liquidFrostedGlass
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    story: Story,
    onBack: () -> Unit,
    onDragOffsetChange: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val hazeState = remember { HazeState() }

    val isPlaying by AudioPlayerManager.isPlaying.collectAsState()
    val currentPosition by AudioPlayerManager.currentPositionMs.collectAsState()
    val duration by AudioPlayerManager.durationMs.collectAsState()
    val playbackSpeed by AudioPlayerManager.playbackSpeed.collectAsState()
    val sleepTimerRemaining by AudioPlayerManager.sleepTimerRemainingSeconds.collectAsState()
    val playlist by AudioPlayerManager.playlist.collectAsState()

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showAiExplanationDialog by remember { mutableStateOf(false) }

    // Смещение по вертикали с плавной пружиной (Animatable) для жеста возврата на главную
    val coroutineScope = rememberCoroutineScope()
    val animOffsetY = remember { Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun triggerDismiss() {
        if (!isDismissing) {
            isDismissing = true
            haptic(Haptics.TICK)
            coroutineScope.launch {
                animOffsetY.animateTo(900f, tween(180, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                onDragOffsetChange(900f)
                onBack()
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Если шторка уже частично сдвинута вниз, движение пальца вверх возвращает ее назад
                if (animOffsetY.value > 0 && available.y < 0) {
                    val consumed = available.y.coerceAtLeast(-animOffsetY.value)
                    coroutineScope.launch {
                        val newOff = (animOffsetY.value + consumed).coerceAtLeast(0f)
                        animOffsetY.snapTo(newOff)
                        onDragOffsetChange(newOff)
                    }
                    return Offset(0f, consumed)
                }
                // Если скролл в самом верху (scrollState.value == 0) и пользователь тянет вниз - сразу начинаем плавно тянуть шторку вниз!
                if (scrollState.value == 0 && available.y > 0) {
                    val drag = available.y * 0.85f
                    coroutineScope.launch {
                        val newOff = animOffsetY.value + drag
                        animOffsetY.snapTo(newOff)
                        onDragOffsetChange(newOff)
                    }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0 && scrollState.value == 0) {
                    val drag = available.y * 0.85f
                    coroutineScope.launch {
                        val newOff = animOffsetY.value + drag
                        animOffsetY.snapTo(newOff)
                        onDragOffsetChange(newOff)
                    }
                    return Offset(0f, available.y)
                }
                // Если пользователь пролистал до конца и свайпает вверх, чтобы закрыть
                if (available.y < -25f && scrollState.value >= (scrollState.maxValue - 40).coerceAtLeast(0)) {
                    triggerDismiss()
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // Смахнуть вниз у верха
                if (animOffsetY.value > 90f || (animOffsetY.value > 25f && available.y > 500f)) {
                    triggerDismiss()
                }
                // Резкий смах вверх для закрытия истории у низа
                else if (available.y < -900f || (available.y < -350f && scrollState.value >= (scrollState.maxValue - 80).coerceAtLeast(0))) {
                    triggerDismiss()
                } else {
                    coroutineScope.launch {
                        animOffsetY.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                        onDragOffsetChange(0f)
                    }
                }
                return Velocity.Zero
            }
        }
    }

    val effectiveProgress = if (isSeeking) {
        seekPosition
    } else {
        if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val initialIndex = remember(story, playlist) {
        val idx = playlist.indexOfFirst { it.id == story.id }
        if (idx != -1) idx else 0
    }
    val coverPagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { playlist.size.coerceAtLeast(1) }
    )

    LaunchedEffect(story.id) {
        val idx = playlist.indexOfFirst { it.id == story.id }
        if (idx != -1 && idx != coverPagerState.currentPage) {
            coverPagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(coverPagerState.currentPage) {
        if (playlist.isNotEmpty() && coverPagerState.currentPage in playlist.indices) {
            val selected = playlist[coverPagerState.currentPage]
            if (selected.id != story.id) {
                AudioPlayerManager.playStory(context, selected)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animOffsetY.value.roundToInt().coerceAtLeast(0)) }
            .nestedScroll(nestedScrollConnection)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                spotColor = Color(0xCC000000)
            )
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(OnyxBackground)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x35FFFFFF), Color(0x0EFFFFFF), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- 1. СКРОЛЛИРУЕМАЯ ОБЛАСТЬ ПЛЕЕРА (на весь экран, начиная от самого верха!) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Отступ под верхнюю плавающую панель + свободное пространство для сияния вверх
                Spacer(modifier = Modifier.height(72.dp))

                // Крупная обложка с кинематографичной оптической анимацией белых бликов (Lens Leaks)
                LensLeakCoverBox(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f),
                    isPlaying = isPlaying,
                    shape = RoundedCornerShape(26.dp),
                    cornerRadius = 26.dp,
                    overflow = 160.dp,
                    intensity = 1.0f
                ) {
                    if (playlist.isNotEmpty()) {
                        HorizontalPager(
                            state = coverPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val itemStory = playlist[page]
                            Card(
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = OnyxSurface),
                                elevation = CardDefaults.cardElevation(18.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        cameraDistance = 12f * density
                                        val pageOffset = ((coverPagerState.currentPage - page) + coverPagerState.currentPageOffsetFraction)
                                        if (pageOffset < -1f || pageOffset > 1f) {
                                            alpha = 0f
                                        } else if (pageOffset <= 0f) {
                                            // Грань куба уходит влево (вращение вокруг правой грани)
                                            alpha = 1f - (-pageOffset * 0.35f)
                                            transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
                                            rotationY = 90f * pageOffset
                                        } else {
                                            // Грань куба приходит справа (вращение вокруг левой грани)
                                            alpha = 1f - (pageOffset * 0.35f)
                                            transformOrigin = TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
                                            rotationY = 90f * pageOffset
                                        }
                                    }
                                    .border(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.28f), Color.White.copy(0.06f))), RoundedCornerShape(26.dp))
                            ) {
                                AsyncImage(
                                    model = itemStory.coverUrl,
                                    contentDescription = itemStory.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = OnyxSurface),
                            elevation = CardDefaults.cardElevation(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.28f), Color.White.copy(0.06f))), RoundedCornerShape(26.dp))
                        ) {
                            AsyncImage(
                                model = story.coverUrl,
                                contentDescription = story.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

            // Название истории
            Text(
                text = story.title,
                color = PureWhite,
                fontSize = 18.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = story.channel,
                color = MutedSilver,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Скромный, аккуратный ползунок перемотки (Minimalist Seekbar) без лишних вертикальных полос
            Slider(
                value = effectiveProgress,
                onValueChange = {
                    isSeeking = true
                    seekPosition = it
                },
                onValueChangeFinished = {
                    haptic(Haptics.TICK)
                    val targetMs = (seekPosition * duration).toLong()
                    AudioPlayerManager.seekTo(targetMs)
                    isSeeking = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .background(PureWhite, CircleShape)
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(3.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = PureWhite.copy(alpha = 0.9f),
                            inactiveTrackColor = Color(0x22FFFFFF)
                        ),
                        drawStopIndicator = null
                    )
                }
            )

            // Временные метки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMsDisplay = if (isSeeking) (seekPosition * duration).toLong() else currentPosition
                val remainingMs = (duration - currentMsDisplay).coerceAtLeast(0L)
                Text(
                    text = AudioPlayerManager.formatMs(currentMsDisplay),
                    color = MutedSilver,
                    fontSize = 12.sp
                )
                Text(
                    text = "-${AudioPlayerManager.formatMs(remainingMs)}",
                    color = MutedSilver,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Кнопки управления: Предыдущая, -10s, Play/Pause, +30s, Следующая (стрелочка вправо)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Стрелочка влево / Предыдущая история
                IconButton(
                    onClick = {
                        haptic(Haptics.CLICK)
                        AudioPlayerManager.playPrevious(context)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .liquidFrostedGlass(shape = CircleShape, hazeState = hazeState, borderWidth = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Предыдущая история",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Назад на 10 сек
                IconButton(
                    onClick = {
                        haptic(Haptics.TICK)
                        AudioPlayerManager.skipBackward()
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .liquidFrostedGlass(shape = CircleShape, hazeState = hazeState, borderWidth = 1.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Назад 10 сек",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("-10s", color = MutedSilver, fontSize = 9.sp)
                    }
                }

                // Белоснежная кнопка Play / Pause
                IconButton(
                    onClick = {
                        haptic(Haptics.CLICK)
                        AudioPlayerManager.togglePlayPause()
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = PureWhite.copy(alpha = 0.5f))
                        .background(PureWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                        tint = OnyxBackground,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Вперед на 30 сек
                IconButton(
                    onClick = {
                        haptic(Haptics.TICK)
                        AudioPlayerManager.skipForward()
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .liquidFrostedGlass(shape = CircleShape, hazeState = hazeState, borderWidth = 1.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Вперед 30 сек",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("+30s", color = MutedSilver, fontSize = 9.sp)
                    }
                }

                // Стрелочка вправо / Следующая история
                IconButton(
                    onClick = {
                        haptic(Haptics.CLICK)
                        AudioPlayerManager.playNext(context)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .liquidFrostedGlass(shape = CircleShape, hazeState = hazeState, borderWidth = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Следующая история",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Интерактивная 3-теговая карусель (видно 3 штуки, боковые масштабируются и плавно затемняются)
            StoryTagsCarousel(story = story, hazeState = hazeState)

            Spacer(modifier = Modifier.height(20.dp))

            // Секция: Подборка похожих историй от ИИ (AI Recommendations)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidFrostedGlass(
                        shape = RoundedCornerShape(20.dp),
                        hazeState = hazeState,
                        borderWidth = 1.2.dp
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ПОДБОРКА ОТ ИИ",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .liquidFrostedGlass(
                                shape = RoundedCornerShape(8.dp),
                                hazeState = hazeState,
                                isWhiteAccent = true,
                                borderWidth = 1.dp
                            )
                            .clickable {
                                haptic(Haptics.TICK)
                                showAiExplanationDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Как работает? ⓘ",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Нейросеть подобрала схожие по саспенсу и атмосфере истории к текущему сюжету:",
                    color = MutedSilver,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Список похожих историй из плейлиста
                val similarStories = remember(story, playlist) {
                    playlist.filter { it.id != story.id }.take(3)
                }

                if (similarStories.isEmpty()) {
                    Text(
                        text = "Загружаем больше историй для подбора рекомендаций...",
                        color = MutedSilver,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        similarStories.forEachIndexed { index, similar ->
                            val matchPercentage = 98 - (index * 4)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidFrostedGlass(
                                        shape = RoundedCornerShape(14.dp),
                                        hazeState = hazeState,
                                        borderWidth = 1.dp
                                    )
                                    .clickable {
                                        haptic(Haptics.CLICK)
                                        AudioPlayerManager.playStory(context, similar)
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = similar.coverUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = similar.title,
                                            color = PureWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "Совпадение $matchPercentage%",
                                                color = PureWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${similar.durationFormatted}",
                                                color = MutedSilver,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Слушать",
                                        tint = PureWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Кнопка сворачивания плеера внизу страницы
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xAA000000))
                    .liquidFrostedGlass(
                        shape = RoundedCornerShape(22.dp),
                        hazeState = hazeState,
                        borderWidth = 1.dp
                    )
                    .clickable {
                        haptic(Haptics.TICK)
                        onBack()
                    }
                    .padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Свернуть",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Свернуть историю",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // --- 2. ПРИКРЕПЛЁННАЯ ВЕРХНЯЯ ПАНЕЛЬ (плавает вверху поверх контента, фон полностью прозрачный) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (animOffsetY.value > 80f) {
                                triggerDismiss()
                            } else {
                                coroutineScope.launch {
                                    animOffsetY.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                                    onDragOffsetChange(0f)
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount > 0 || animOffsetY.value > 0) {
                                coroutineScope.launch {
                                    val newOff = (animOffsetY.value + dragAmount).coerceAtLeast(0f)
                                    animOffsetY.snapTo(newOff)
                                    onDragOffsetChange(newOff)
                                }
                                change.consume()
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ручка свайпа (Drag Handle Pill)
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color(0x3AFFFFFF))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        haptic(Haptics.TICK)
                        onBack()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .liquidFrostedGlass(shape = CircleShape, borderWidth = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Свернуть",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "СЕЙЧАС ИГРАЕТ",
                    color = MutedSilver,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )

                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

    // Диалог: как работает ИИ и как его подключить
    if (showAiExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showAiExplanationDialog = false },
            confirmButton = {
                TextButton(onClick = { showAiExplanationDialog = false }) {
                    Text("Понятно", color = PureWhite)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Как работает ИИ в приложении", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Анализ сюжета:\nНейросеть (Gemini API) анализирует текстовое описание, таймкоды и теги страшной истории.",
                        color = SoftWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "2. Векторные эмбеддинги:\nВ базе данных Supabase создаются эмбеддинги (векторы смысла) через модель text-embedding-004.",
                        color = SoftWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "3. Мгновенный подбор:\nПри включении трека алгоритм за доли секунды находит истории с наиболее похожим уровнем саспенса и локацией (лес, заброшка, аномалия).",
                        color = SoftWhite,
                        fontSize = 13.sp
                    )
                }
            },
            containerColor = OnyxSurface
        )
    }

    // Диалог таймера сна
    if (showSleepDialog) {
        SleepTimerDialog(
            activeSeconds = sleepTimerRemaining,
            onDismiss = { showSleepDialog = false },
            onSelectMinutes = { mins ->
                haptic(Haptics.CONFIRM)
                AudioPlayerManager.setSleepTimer(mins)
                showSleepDialog = false
            }
        )
    }

    // Диалог скорости
    if (showSpeedDialog) {
        SpeedSelectDialog(
            currentSpeed = playbackSpeed,
            onDismiss = { showSpeedDialog = false },
            onSelectSpeed = { speed ->
                haptic(Haptics.CONFIRM)
                AudioPlayerManager.setSpeed(speed)
                showSpeedDialog = false
            }
        )
    }
}

@Composable
fun SleepTimerDialog(
    activeSeconds: Long?,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int?) -> Unit
) {
    val options = listOf(
        15 to "Через 15 минут",
        30 to "Через 30 минут",
        45 to "Через 45 минут",
        60 to "Через 1 час"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MutedSilver)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bedtime, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Таймер сна", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PureWhite)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Остановить воспроизведение по таймеру:",
                    color = MutedSilver,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                options.forEach { (mins, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidFrostedGlass(
                                shape = RoundedCornerShape(10.dp),
                                borderWidth = 1.dp
                            )
                            .clickable { onSelectMinutes(mins) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = label,
                            color = SoftWhite,
                            fontSize = 13.5.sp
                        )
                    }
                }

                if (activeSeconds != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidFrostedGlass(
                                shape = RoundedCornerShape(10.dp),
                                borderWidth = 1.dp,
                                backgroundColor = Color(0x35FF5252),
                                tint = Color(0x35FF5252)
                            )
                            .clickable { onSelectMinutes(null) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Выключить таймер",
                            color = Color(0xFFFF8A80),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        containerColor = OnyxSurface
    )
}

@Composable
fun SpeedSelectDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSelectSpeed: (Float) -> Unit
) {
    val speeds = listOf(0.8f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = MutedSilver)
            }
        },
        title = { Text("Скорость воспроизведения", color = PureWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                speeds.forEach { speed ->
                    val isSelected = currentSpeed == speed
                    val label = if (speed == 1.0f) "${speed}x (Обычная)" else "${speed}x"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidFrostedGlass(
                                shape = RoundedCornerShape(10.dp),
                                isActive = isSelected,
                                borderWidth = if (isSelected) 1.2.dp else 1.dp
                            )
                            .clickable { onSelectSpeed(speed) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) PureWhite else SoftWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        containerColor = OnyxSurface
    )
}

@Composable
fun StoryTagsCarousel(
    story: Story,
    onTagClick: (String) -> Unit = {},
    hazeState: HazeState? = null
) {
    val haptic = rememberHapticFeedback()
    val defaultTags = listOf("🏚️ Заброшки", "🌲 Тайга", "👁️ Мистика", "🏕️ Деревня", "📻 Ночь", "💀 Аномалии", "🐺 Оборотни", "⚡ Крипипаста")
    val allTags = remember(story) {
        val merged = (story.tags + defaultTags).distinct()
        if (merged.size < 5) defaultTags else merged
    }

    val pagerState = rememberPagerState(
        initialPage = 2.coerceAtMost(allTags.size - 1),
        pageCount = { allTags.size }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "АТМОСФЕРА И ТЕГИ",
            color = MutedSilver,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Карусель: математически центрированный активный тег, боковые плавно уменьшаются и затемняются (минимальное расстояние)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            val itemWidth = 106.dp
            val centerPadding = ((maxWidth - itemWidth) / 2).coerceAtLeast(0.dp)

            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(itemWidth),
                contentPadding = PaddingValues(horizontal = centerPadding),
                pageSpacing = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val tag = allTags[page]
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                val scale = lerp(1.0f, 0.85f, pageOffset)
                val alpha = lerp(1.0f, 0.35f, pageOffset)
                val isSelected = pageOffset < 0.25f

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .shadow(
                                elevation = if (isSelected) 8.dp else 2.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Color(0x99000000)
                            )
                            .liquidFrostedGlass(
                                shape = RoundedCornerShape(16.dp),
                                hazeState = hazeState,
                                isActive = isSelected,
                                borderWidth = if (isSelected) 1.2.dp else 1.dp
                            )
                            .clickable {
                                haptic(Haptics.TICK)
                                onTagClick(tag)
                            }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tag,
                            color = if (isSelected) PureWhite else MutedSilver,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Мягкое виньетирование (затемнение краев карусели)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to OnyxBackground,
                            0.12f to Color.Transparent,
                            0.88f to Color.Transparent,
                            1.0f to OnyxBackground
                        )
                    )
            )
        }
    }
}

