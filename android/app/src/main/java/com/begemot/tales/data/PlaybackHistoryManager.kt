package com.begemot.tales.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackHistoryManager {
    private const val PREFS_NAME = "kot_begemot_playback_history"
    private const val KEY_POS_PREFIX = "pos_"
    private const val KEY_DUR_PREFIX = "dur_"

    private var prefs: SharedPreferences? = null

    private val _historyVersion = MutableStateFlow(0)
    val historyVersion = _historyVersion.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveProgress(storyId: String, positionMs: Long, durationMs: Long) {
        val p = prefs ?: return
        if (durationMs <= 0) return
        p.edit()
            .putLong(KEY_POS_PREFIX + storyId, positionMs)
            .putLong(KEY_DUR_PREFIX + storyId, durationMs)
            .apply()
        _historyVersion.value += 1
    }

    fun getProgressFraction(storyId: String): Float? {
        val p = prefs ?: return null
        val pos = p.getLong(KEY_POS_PREFIX + storyId, 0L)
        val dur = p.getLong(KEY_DUR_PREFIX + storyId, 0L)
        if (dur <= 0L || pos <= 0L) return null
        return (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
    }

    fun getSavedPosition(storyId: String): Long {
        val p = prefs ?: return 0L
        return p.getLong(KEY_POS_PREFIX + storyId, 0L)
    }

    fun getSavedDuration(storyId: String): Long {
        val p = prefs ?: return 0L
        return p.getLong(KEY_DUR_PREFIX + storyId, 0L)
    }
}

