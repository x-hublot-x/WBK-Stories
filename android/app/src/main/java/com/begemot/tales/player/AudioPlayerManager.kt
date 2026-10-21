package com.begemot.tales.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.begemot.tales.data.PlaybackHistoryManager
import com.begemot.tales.data.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AudioPlayerManager {
    private var exoPlayer: ExoPlayer? = null

    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory = _currentStory.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.asStateFlow()

    private val _playlist = MutableStateFlow<List<Story>>(emptyList())
    val playlist = _playlist.asStateFlow()

    private var applicationContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        PlaybackHistoryManager.initialize(context)
        getOrCreatePlayer(context)
    }

    fun updatePlaylist(stories: List<Story>) {
        _playlist.value = stories
    }

    fun playNext(context: Context? = null) {
        val ctx = context ?: applicationContext ?: return
        val current = _currentStory.value ?: return
        val list = _playlist.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx != -1 && idx + 1 < list.size) {
            playStory(ctx, list[idx + 1])
        }
    }

    fun playPrevious(context: Context? = null) {
        val ctx = context ?: applicationContext ?: return
        val current = _currentStory.value ?: return
        val list = _playlist.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx > 0) {
            playStory(ctx, list[idx - 1])
        }
    }

    @OptIn(UnstableApi::class)
    fun getOrCreatePlayer(context: Context, audioAttributes: AudioAttributes? = null): ExoPlayer {
        return exoPlayer ?: run {
            val attrs = audioAttributes ?: AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build()

            val player = ExoPlayer.Builder(context.applicationContext)
                .setAudioAttributes(attrs, true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .setSeekBackIncrementMs(10000L)
                .setSeekForwardIncrementMs(30000L)
                .build()

            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                    if (playing) {
                        startProgressTracking()
                    } else {
                        stopProgressTracking()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _durationMs.value = player.duration.coerceAtLeast(0L)
                    } else if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        _currentPositionMs.value = 0L
                        _currentStory.value?.let { story ->
                            val dur = _durationMs.value
                            if (dur > 0) {
                                PlaybackHistoryManager.saveProgress(story.id, dur, dur)
                            }
                        }
                        stopProgressTracking()
                    }
                }
            })

            exoPlayer = player
            player
        }
    }

    fun playStory(context: Context, story: Story) {
        _currentStory.value = story
        val player = getOrCreatePlayer(context)

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(story.title)
            .setArtist(story.channel)
            .setDisplayTitle(story.title)
            .apply {
                if (!story.coverUrl.isNullOrEmpty()) {
                    setArtworkUri(Uri.parse(story.coverUrl))
                }
            }
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(story.id)
            .setUri(story.audioUrl)
            .setMediaMetadata(mediaMetadata)
            .build()

        val savedPos = PlaybackHistoryManager.getSavedPosition(story.id)
        val savedDur = PlaybackHistoryManager.getSavedDuration(story.id)
        val isNearEnd = savedDur > 0 && (savedPos >= savedDur - 15000L || (savedPos.toFloat() / savedDur.toFloat()) >= 0.95f)

        player.setMediaItem(mediaItem)
        player.prepare()
        if (savedPos > 5000L && !isNearEnd) {
            player.seekTo(savedPos)
            _currentPositionMs.value = savedPos
        } else {
            _currentPositionMs.value = 0L
        }
        player.play()

        // Запускаем Foreground Service для шторки уведомлений и работы в фоне
        try {
            val serviceIntent = Intent(context.applicationContext, PlaybackService::class.java)
            ContextCompat.startForegroundService(context.applicationContext, serviceIntent)
        } catch (_: Exception) {}
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _currentStory.value?.let { story ->
                if (player.duration > 0) {
                    PlaybackHistoryManager.saveProgress(story.id, player.currentPosition, player.duration)
                }
            }
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs)
        _currentPositionMs.value = positionMs
        _currentStory.value?.let { story ->
            if (player.duration > 0) {
                PlaybackHistoryManager.saveProgress(story.id, positionMs, player.duration)
            }
        }
    }

    fun skipForward() {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + 30000L).coerceAtMost(player.duration.coerceAtLeast(0L))
        seekTo(newPos)
    }

    fun skipBackward() {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition - 10000L).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun setSpeed(speed: Float) {
        val player = exoPlayer ?: return
        _playbackSpeed.value = speed
        player.playbackParameters = PlaybackParameters(speed)
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _sleepTimerRemainingSeconds.value = null
            return
        }

        var totalSeconds = minutes * 60L
        _sleepTimerRemainingSeconds.value = totalSeconds

        sleepTimerJob = scope.launch {
            while (isActive && totalSeconds > 0) {
                delay(1000L)
                totalSeconds--
                _sleepTimerRemainingSeconds.value = totalSeconds
            }
            exoPlayer?.pause()
            _sleepTimerRemainingSeconds.value = null
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var tickCount = 0
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = player.duration
                    _currentPositionMs.value = pos
                    if (dur > 0) {
                        _durationMs.value = dur
                        // Периодически сохраняем прогресс (~ раз в 1.2 сек)
                        if (tickCount % 3 == 0) {
                            _currentStory.value?.let { story ->
                                PlaybackHistoryManager.saveProgress(story.id, pos, dur)
                            }
                        }
                    }
                }
                tickCount++
                delay(400L)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        val h = m / 60
        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m % 60, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }
}
