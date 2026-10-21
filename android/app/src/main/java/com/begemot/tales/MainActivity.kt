package com.begemot.tales

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.begemot.tales.player.AudioPlayerManager
import com.begemot.tales.theme.OnyxBackground
import com.begemot.tales.theme.KotBegemotTalesTheme
import com.begemot.tales.ui.CatalogScreen
import com.begemot.tales.ui.PlayerScreen

sealed class AppScreen {
    data object Catalog : AppScreen()
    data object Player : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Подключаем MediaController к сервису воспроизведения
        AudioPlayerManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            KotBegemotTalesTheme {
                // Запрос разрешения на уведомления для Android 13+
                NotificationPermissionEffect()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.begemot.tales.theme.OnyxBackground
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPermissionEffect() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { /* Разрешение получено или отклонено */ }
        )
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun AppNavigation() {
    var isPlayerOpen by remember { mutableStateOf(false) }
    var playerDragOffsetY by remember { mutableFloatStateOf(0f) }
    val currentStory by AudioPlayerManager.currentStory.collectAsState()

    BackHandler(enabled = isPlayerOpen) {
        isPlayerOpen = false
        playerDragOffsetY = 0f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Динамический расчет scrim и scale каталога при открытом плеере и при жесте смахивания
        val dragFraction = (playerDragOffsetY / 700f).coerceIn(0f, 1f)
        val targetScrim = if (isPlayerOpen) (0.65f * (1f - dragFraction * 0.85f)) else 0f
        val scrimAlpha by animateFloatAsState(
            targetValue = targetScrim,
            animationSpec = tween(180, easing = FastOutSlowInEasing),
            label = "scrimAlpha"
        )
        val targetScale = if (isPlayerOpen) (0.94f + 0.06f * dragFraction) else 1.0f
        val catalogScale by animateFloatAsState(
            targetValue = targetScale,
            animationSpec = tween(180, easing = FastOutSlowInEasing),
            label = "catalogScale"
        )

        // Главная страница каталога всегда остаётся активной на заднем плане
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = catalogScale
                    scaleY = catalogScale
                    transformOrigin = TransformOrigin(0.5f, 0.35f)
                }
        ) {
            CatalogScreen(
                onOpenPlayer = {
                    playerDragOffsetY = 0f
                    isPlayerOpen = true
                }
            )
        }

        // Плавный затемняющий слой (scrim) поверх каталога при открытом плеере
        if (scrimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isPlayerOpen = false
                        playerDragOffsetY = 0f
                    }
            )
        }

        // Полноэкранный плеер поверх каталога со скольжением снизу вверх
        AnimatedVisibility(
            visible = isPlayerOpen && currentStory != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(tween(180)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = FastOutLinearInEasing)
            ) + fadeOut(tween(160))
        ) {
            currentStory?.let { story ->
                PlayerScreen(
                    story = story,
                    onBack = {
                        isPlayerOpen = false
                        playerDragOffsetY = 0f
                    },
                    onDragOffsetChange = { offset ->
                        playerDragOffsetY = offset
                    }
                )
            }
        }
    }
}
