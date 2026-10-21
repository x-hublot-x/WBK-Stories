package com.begemot.tales.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionResult
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.begemot.tales.MainActivity
import com.begemot.tales.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var cachedBitmap: Bitmap? = null
    private var cachedCoverUrl: String? = null

    companion object {
        const val CHANNEL_ID = "kot_begemot_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Создаем канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Воспроизведение историй",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Управление воспроизведением плеера"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val p = AudioPlayerManager.getOrCreatePlayer(this, audioAttributes)
        player = p

        val forwardingPlayer = PlaybackForwardingPlayer(
            p,
            onNext = { AudioPlayerManager.playNext(this@PlaybackService) },
            onPrevious = { AudioPlayerManager.playPrevious(this@PlaybackService) }
        )

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                        .add(Player.COMMAND_SEEK_TO_NEXT)
                        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .build()
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailablePlayerCommands(playerCommands)
                        .build()
                }

                override fun onPlayerCommandRequest(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    playerCommand: Int
                ): Int {
                    if (playerCommand == Player.COMMAND_SEEK_TO_NEXT || playerCommand == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM) {
                        AudioPlayerManager.playNext(this@PlaybackService)
                        return SessionResult.RESULT_SUCCESS
                    }
                    if (playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS || playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM) {
                        AudioPlayerManager.playPrevious(this@PlaybackService)
                        return SessionResult.RESULT_SUCCESS
                    }
                    return super.onPlayerCommandRequest(session, controller, playerCommand)
                }
            })
            .build()

        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
            override fun onPlaybackStateChanged(state: Int) {
                updateNotification()
            }
        })

        // Подписываемся на смену истории для динамической загрузки обложки
        serviceScope.launch {
            AudioPlayerManager.currentStory.collect {
                loadCoverAndNotify()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildMediaNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaybackService", "Error in startForeground", e)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, buildMediaNotification())
    }

    private fun loadCoverAndNotify() {
        val story = AudioPlayerManager.currentStory.value ?: return
        val url = story.coverUrl
        if (url.isNullOrEmpty() || url == cachedCoverUrl) {
            updateNotification()
            return
        }

        cachedCoverUrl = url
        serviceScope.launch {
            try {
                val loader = ImageLoader(this@PlaybackService)
                val req = ImageRequest.Builder(this@PlaybackService)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(req) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    cachedBitmap = bitmap
                    updateNotification()
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaybackService", "Failed to load cover art", e)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildMediaNotification(): Notification {
        val currentStory = AudioPlayerManager.currentStory.value
        val title = currentStory?.title ?: "WorldBegemotKot"
        val artist = currentStory?.channel ?: "Страшные истории"

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(sessionActivityPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(player?.isPlaying == true)

        cachedBitmap?.let {
            builder.setLargeIcon(it)
        }

        mediaSession?.let { session ->
            builder.setStyle(MediaStyleNotificationHelper.MediaStyle(session))
        }

        return builder.build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
