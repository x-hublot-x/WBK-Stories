package com.begemot.tales.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import coil.compose.AsyncImage
import com.begemot.tales.data.PlaybackHistoryManager
import com.begemot.tales.data.StoriesRepository
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
import com.begemot.tales.theme.SubtleGrey
import com.begemot.tales.theme.liquidFrostedGlass
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

data class TagCategory(
    val label: String,
    val query: String = ""
)

val CATEGORIES = listOf(
    TagCategory("⚡ Все", ""),
    TagCategory("🏚️ Заброшки", "заброш"),
    TagCategory("🌲 Тайга", "тайг"),
    TagCategory("👁️ Мистика", "мистик"),
    TagCategory("🏕️ Деревня", "деревн"),
    TagCategory("📻 Ночь", "ночь"),
    TagCategory("💀 Аномалии", "аномал"),
    TagCategory("🐺 Оборотни", "оборотн")
)

@Composable
fun CatalogScreen(
    onOpenPlayer: () -> Unit
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val hazeState = remember { HazeState() }

    var stories by remember { mutableStateOf<List<Story>>(StoriesRepository.cachedStories) }
    var isLoading by remember { mutableStateOf(stories.isEmpty()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CATEGORIES.first()) }
    var isDurationFilterActive by remember { mutableStateOf(false) }
    var customMinMinutes by remember { mutableFloatStateOf(10f) }
    var customMaxMinutes by remember { mutableFloatStateOf(45f) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val currentStory by AudioPlayerManager.currentStory.collectAsState()
    val isPlaying by AudioPlayerManager.isPlaying.collectAsState()
    val currentPosition by AudioPlayerManager.currentPositionMs.collectAsState()
    val duration by AudioPlayerManager.durationMs.collectAsState()
    val historyVersion by PlaybackHistoryManager.historyVersion.collectAsState()
    val playlist by AudioPlayerManager.playlist.collectAsState()

    fun getStoryDurationMinutes(story: Story): Long {
        val parts = story.durationFormatted.split(":")
        return try {
            if (parts.size == 2) {
                val min = parts[0].toLongOrNull() ?: 0L
                val sec = parts[1].toLongOrNull() ?: 0L
                min + if (sec >= 30) 1 else 0
            } else if (parts.size == 3) {
                val hr = parts[0].toLongOrNull() ?: 0L
                val min = parts[1].toLongOrNull() ?: 0L
                hr * 60 + min
            } else {
                (story.durationSeconds / 60).coerceAtLeast(1)
            }
        } catch (e: Exception) {
            (story.durationSeconds / 60).coerceAtLeast(1)
        }
    }

    fun loadStories() {
        isLoading = true
        coroutineScope.launch {
            val result = StoriesRepository.getStories(forceRefresh = true)
            result.onSuccess {
                stories = it
                AudioPlayerManager.updatePlaylist(it)
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadStories()
    }

    val filteredStories = remember(stories, searchQuery, selectedCategory, isDurationFilterActive, customMinMinutes, customMaxMinutes) {
        stories.filter { story ->
            val storyDurationMin = getStoryDurationMinutes(story)
            val matchesDuration = if (isDurationFilterActive) {
                storyDurationMin >= customMinMinutes.toLong() && storyDurationMin <= customMaxMinutes.toLong()
            } else {
                true
            }

            val matchesCategory = selectedCategory.query.isEmpty() ||
                story.title.contains(selectedCategory.query, ignoreCase = true) ||
                story.description.contains(selectedCategory.query, ignoreCase = true) ||
                story.tags.any { it.contains(selectedCategory.query, ignoreCase = true) }

            val matchesSearch = searchQuery.isBlank() ||
                story.title.contains(searchQuery, ignoreCase = true) ||
                story.tags.any { it.contains(searchQuery, ignoreCase = true) }

            matchesDuration && matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBackground)
    ) {
        // Контентная область: карточки на весь экран, начинаются под шапкой и плавно уходят ПОД парящие острова
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(36.dp), strokeWidth = 2.5.dp)
            }
        } else if (filteredStories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("В этой категории историй нет", color = MutedSilver, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 175.dp, // Карточки плавно стартуют прямо под островами и скроллятся ПОД ними
                    bottom = if (currentStory != null) 96.dp else 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStories, key = { it.id }) { story ->
                    val isCurrent = currentStory?.id == story.id
                    GlassStoryCard(
                        story = story,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        historyVersion = historyVersion,
                        onAvatarClick = {
                            haptic(Haptics.CLICK)
                            if (isCurrent) {
                                AudioPlayerManager.togglePlayPause()
                            } else {
                                AudioPlayerManager.playStory(context, story)
                            }
                        },
                        onClick = {
                            haptic(Haptics.CLICK)
                            if (currentStory?.id != story.id) {
                                AudioPlayerManager.playStory(context, story)
                            }
                            onOpenPlayer()
                        }
                    )
                }
            }
        }

        // Парящий остров шапки (Liquid Glass Header) с мягким полупрозрачным градиентом растворения
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.0f to OnyxBackground.copy(alpha = 0.98f),
                        0.76f to OnyxBackground.copy(alpha = 0.88f),
                        1.0f to OnyxBackground.copy(alpha = 0.0f)
                    )
                )
                .padding(bottom = 12.dp)
        ) {
            // Премиум-шапка (Dark Onyx Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(PureWhite, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "WORLD BEGEMOT KOT",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = "Официальные хоррор-истории",
                        color = MutedSilver,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic(Haptics.TICK)
                        loadStories()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .liquidFrostedGlass(shape = CircleShape, borderWidth = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = SoftWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Отдельный парящий остров: Поисковая строка + Кнопка настраиваемого фильтра длительности
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingGlassSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    hazeState = hazeState,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                DurationFilterButton(
                    isActive = isDurationFilterActive,
                    minMinutes = customMinMinutes.toInt(),
                    maxMinutes = customMaxMinutes.toInt(),
                    hazeState = hazeState,
                    onClick = {
                        haptic(Haptics.CLICK)
                        showCustomDurationDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Отдельный парящий остров: Теги и Категории Liquid Glass (чистые темы, без таймингов)
            FloatingGlassCategoriesBar(
                categories = CATEGORIES,
                selectedCategory = selectedCategory,
                hazeState = hazeState,
                onSelectCategory = { cat ->
                    haptic(Haptics.TICK)
                    selectedCategory = cat
                },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Отдельный парящий плеер внизу страницы (Liquid Glass остров)
        AnimatedVisibility(
            visible = currentStory != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(280, easing = FastOutLinearInEasing)
            ) + fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        ) {
            currentStory?.let { story ->
                FloatingGlassPlayer(
                    story = story,
                    playlist = if (playlist.isNotEmpty()) playlist else stories,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    hazeState = hazeState,
                    onClick = {
                        haptic(Haptics.CLICK)
                        onOpenPlayer()
                    },
                    onTogglePlay = {
                        haptic(Haptics.CLICK)
                        AudioPlayerManager.togglePlayPause()
                    },
                    onStoryChange = { newStory ->
                        haptic(Haptics.TICK)
                        AudioPlayerManager.playStory(context, newStory)
                    }
                )
            }
        }
    }

    if (showCustomDurationDialog) {
        DurationFilterDialog(
            isActive = isDurationFilterActive,
            currentMin = customMinMinutes,
            currentMax = customMaxMinutes,
            onDismiss = { showCustomDurationDialog = false },
            onApply = { active, min, max ->
                isDurationFilterActive = active
                customMinMinutes = min
                customMaxMinutes = max
                showCustomDurationDialog = false
            }
        )
    }
}

@Composable
fun GlassStoryCard(
    story: Story,
    isCurrent: Boolean,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    historyVersion: Int,
    onAvatarClick: () -> Unit,
    onClick: () -> Unit
) {
    val savedProgress = if (isCurrent && duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        PlaybackHistoryManager.getProgressFraction(story.id)
    }

    val savedPos = if (isCurrent) currentPosition else PlaybackHistoryManager.getSavedPosition(story.id)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidFrostedGlass(
                shape = RoundedCornerShape(18.dp),
                isActive = isCurrent,
                borderWidth = if (isCurrent) 1.2.dp else 1.dp
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Квадратная интерактивная обложка (аватарка) с оптическими бликами и прямым запуском
            LensLeakCoverBox(
                modifier = Modifier.size(64.dp),
                isPlaying = isCurrent && isPlaying,
                shape = RoundedCornerShape(12.dp),
                cornerRadius = 12.dp,
                overflow = 48.dp,
                intensity = 0.85f
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF101012))
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = story.coverUrl,
                        contentDescription = story.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlaying) {
                                AnimatedSoundWaveBars()
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Играть",
                                    tint = PureWhite,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Играть",
                                tint = PureWhite.copy(alpha = 0.85f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // YouTube-стиль: красная линия прогресса внизу миниатюры
                    if (savedProgress != null && savedProgress > 0.015f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Color(0x99000000))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(savedProgress.coerceIn(0f, 1f))
                                    .height(3.5.dp)
                                    .background(Color(0xFFFF0000))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Текстовый блок
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.title,
                    color = if (isCurrent) PureWhite else SoftWhite,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MutedSilver,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = story.durationFormatted.ifEmpty { "25:24" },
                        color = MutedSilver,
                        fontSize = 12.sp
                    )

                    // Отметка о тайминге и прогрессе прослушивания в стиле YouTube
                    val totalStoryMs = if (isCurrent && duration > 0) duration else story.durationSeconds * 1000L
                    val remainingMs = (totalStoryMs - savedPos).coerceAtLeast(0L)
                    if (savedProgress != null) {
                        if (savedProgress >= 0.95f) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Прослушано",
                                color = Color(0xFFFF4444),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (savedProgress > 0.02f && remainingMs > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ост. ${AudioPlayerManager.formatMs(remainingMs)}",
                                color = Color(0xFFFF5252),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (story.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•  ${story.tags.first()}",
                            color = PureWhite.copy(alpha = 0.7f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSoundWaveBars() {
    val transition = rememberInfiniteTransition(label = "wave")
    val h1 by transition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 18f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 6f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(h1.dp).background(PureWhite, CircleShape))
        Box(modifier = Modifier.width(3.dp).height(h2.dp).background(PureWhite, CircleShape))
        Box(modifier = Modifier.width(3.dp).height(h3.dp).background(PureWhite, CircleShape))
    }
}

@Composable
fun FloatingGlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val haptic = rememberHapticFeedback()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xBB000000),
                ambientColor = Color(0xFF000000)
            )
            .liquidFrostedGlass(
                shape = RoundedCornerShape(22.dp),
                hazeState = hazeState,
                borderWidth = 1.2.dp
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MutedSilver,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Поиск историй...",
                        color = SubtleGrey,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(PureWhite),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Очистить",
                    tint = MutedSilver,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            haptic(Haptics.TICK)
                            onQueryChange("")
                        }
                )
            }
        }
    }
}

@Composable
fun FloatingGlassCategoriesBar(
    categories: List<TagCategory>,
    selectedCategory: TagCategory,
    onSelectCategory: (TagCategory) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val haptic = rememberHapticFeedback()

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = if (isSelected) 8.dp else 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = if (isSelected) Color(0x99000000) else Color(0x55000000)
                        )
                        .liquidFrostedGlass(
                            shape = RoundedCornerShape(18.dp),
                            hazeState = hazeState,
                            isActive = isSelected,
                            borderWidth = if (isSelected) 1.2.dp else 1.dp
                        )
                        .clickable {
                            haptic(Haptics.TICK)
                            onSelectCategory(category)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category.label,
                        color = if (isSelected) PureWhite else MutedSilver,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Мягкое виньетирование краев экрана (плавное растворение без жесткого срезания чипов)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(16.dp)
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(OnyxBackground.copy(alpha = 0.82f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(16.dp)
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, OnyxBackground.copy(alpha = 0.82f))
                    )
                )
        )
    }
}

@Composable
fun FloatingGlassPlayer(
    story: Story,
    playlist: List<Story>,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onStoryChange: (Story) -> Unit,
    hazeState: HazeState? = null
) {
    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val displayList = if (playlist.isNotEmpty()) playlist else listOf(story)
    val initialIndex = remember(story, displayList) {
        val idx = displayList.indexOfFirst { it.id == story.id }
        if (idx != -1) idx else 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { displayList.size }
    )

    LaunchedEffect(story.id) {
        val idx = displayList.indexOfFirst { it.id == story.id }
        if (idx != -1 && idx != pagerState.currentPage) {
            pagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (displayList.isNotEmpty() && pagerState.settledPage in displayList.indices) {
            val selected = displayList[pagerState.settledPage]
            if (selected.id != story.id) {
                onStoryChange(selected)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0xCC000000),
                ambientColor = Color(0xFF000000)
            )
            .liquidFrostedGlass(
                shape = RoundedCornerShape(26.dp),
                hazeState = hazeState,
                borderWidth = 1.2.dp
            )
    ) {
        Column {
            // Тончайшая линия воспроизведения на верхнем краю пилюли
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = PureWhite.copy(alpha = 0.9f),
                trackColor = Color(0x18FFFFFF)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Плавное перелистывание историй с элегантным горизонтальным параллаксом и фейдом
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val pageStory = displayList[page]
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
                    val alpha = lerp(1f, 0.15f, absOffset)
                    val scale = lerp(1f, 0.92f, absOffset)
                    val slideX = pageOffset * 22f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                this.alpha = alpha
                                scaleX = scale
                                scaleY = scale
                                translationX = slideX * density
                            }
                            .clickable { onClick() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LensLeakCoverBox(
                            modifier = Modifier.size(44.dp),
                            isPlaying = isPlaying && pageStory.id == story.id,
                            shape = RoundedCornerShape(12.dp),
                            cornerRadius = 12.dp,
                            overflow = 36.dp,
                            intensity = 0.80f
                        ) {
                            AsyncImage(
                                model = pageStory.coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(0.28f), Color.White.copy(0.06f))), RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(13.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pageStory.title,
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (pageStory.id == story.id) {
                                    "${AudioPlayerManager.formatMs(currentPosition)} / ${story.durationFormatted}"
                                } else {
                                    pageStory.durationFormatted.ifEmpty { "История" }
                                },
                                color = MutedSilver,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка Play/Pause (стрелочка переключения следующего трека вынесена в вывод медиаданных телефона)
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape, spotColor = PureWhite.copy(alpha = 0.5f))
                        .background(PureWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Пауза" else "Играть",
                        tint = OnyxBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DurationFilterButton(
    isActive: Boolean,
    minMinutes: Int,
    maxMinutes: Int,
    onClick: () -> Unit,
    hazeState: HazeState? = null
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(
                elevation = if (isActive) 12.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isActive) Color(0x99FFFFFF) else Color(0x77000000)
            )
            .liquidFrostedGlass(
                shape = RoundedCornerShape(18.dp),
                hazeState = hazeState,
                isActive = isActive,
                isWhiteAccent = isActive,
                borderWidth = 1.2.dp
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Фильтр по времени",
                tint = PureWhite,
                modifier = Modifier.size(19.dp)
            )
            if (isActive) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(PureWhite, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationFilterDialog(
    isActive: Boolean,
    currentMin: Float,
    currentMax: Float,
    onDismiss: () -> Unit,
    onApply: (isActive: Boolean, min: Float, max: Float) -> Unit
) {
    var minVal by remember { mutableFloatStateOf(currentMin) }
    var maxVal by remember { mutableFloatStateOf(currentMax) }
    var activeState by remember { mutableStateOf(isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(activeState, minVal, maxVal)
                }
            ) {
                Text("Применить", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onApply(false, 10f, 45f)
                }
            ) {
                Text("Сбросить", color = MutedSilver)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Фильтр длительности",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Быстрый выбор длительности:",
                    color = MutedSilver,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Любое" to null,
                        "< 25 мин" to (0f to 25f),
                        "25–30 мин" to (25f to 30f),
                        "30+ мин" to (30f to 90f)
                    ).forEach { (label, range) ->
                        val isPresetSelected = if (range == null) !activeState else (activeState && minVal == range.first && maxVal == range.second)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .liquidFrostedGlass(
                                    shape = RoundedCornerShape(10.dp),
                                    isActive = isPresetSelected,
                                    isWhiteAccent = isPresetSelected,
                                    borderWidth = 1.dp
                                )
                                .clickable {
                                    if (range == null) {
                                        activeState = false
                                    } else {
                                        activeState = true
                                        minVal = range.first
                                        maxVal = range.second
                                    }
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isPresetSelected) PureWhite else SoftWhite,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Диапазон: ${minVal.toInt()} – ${maxVal.toInt()} мин",
                            color = if (activeState) PureWhite else MutedSilver,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!activeState) {
                            Text(
                                text = "(выключен)",
                                color = SubtleGrey,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Минимум",
                                color = MutedSilver,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "${minVal.toInt()} мин",
                                color = PureWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Slider(
                            value = minVal,
                            onValueChange = {
                                minVal = it.coerceAtMost(maxVal - 3f)
                                activeState = true
                            },
                            valueRange = 0f..60f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp),
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
                                        activeTrackColor = PureWhite,
                                        inactiveTrackColor = Color(0x33FFFFFF)
                                    ),
                                    drawStopIndicator = null
                                )
                            }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Максимум",
                                color = MutedSilver,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "${maxVal.toInt()} мин",
                                color = PureWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Slider(
                            value = maxVal,
                            onValueChange = {
                                maxVal = it.coerceAtLeast(minVal + 3f)
                                activeState = true
                            },
                            valueRange = 10f..90f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp),
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
                                        activeTrackColor = PureWhite,
                                        inactiveTrackColor = Color(0x33FFFFFF)
                                    ),
                                    drawStopIndicator = null
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = OnyxSurface
    )
}
