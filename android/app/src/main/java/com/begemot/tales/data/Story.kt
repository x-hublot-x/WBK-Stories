package com.begemot.tales.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: String,
    val title: String,
    @SerialName("duration_seconds")
    val durationSeconds: Long = 0,
    @SerialName("duration_formatted")
    val durationFormatted: String = "",
    val channel: String = "WorldBegemotKot",
    @SerialName("audio_url")
    val audioUrl: String,
    @SerialName("cover_url")
    val coverUrl: String? = null,
    val description: String = "",
    val tags: List<String> = emptyList()
)
